package com.example.redis.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.RedisStrictCommand;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LockHelper {

    @Resource
    private RedissonClient redissonClient;


    /**
     * 锁过程的详情可见 {@link org.redisson.RedissonLock#tryLockInnerAsync(long, TimeUnit, long, RedisStrictCommand)}
     *
     * KEYS[1] 锁名称
     * ARGV[1] 锁过期时间
     * ARGV[2] 通过线程名称生成的客户端的唯一标识，解锁时需匹配才能解锁，防止误解锁的场景
     *
     * 具体 lua 脚本如下
     *
     * // 判断锁是否存在，
     * if (redis.call('exists', KEYS[1]) == 0)
     *      // 不存在就用 hset 设置锁，并设定 ARGV[2] 字段,解锁时匹配该字段才解锁
     *      // 第一次设定 ARGV[2] 字段对应的数值为 1，此数值代表可重入次数
     *      then redis.call('hset', KEYS[1], ARGV[2], 1);
     *      // 给锁设定过期时间
     *      redis.call('pexpire', KEYS[1], ARGV[1]);
     *      return nil;
     * end;
     * // 锁存在
     * // 判断 ARGV[2] 是否匹配，匹配说明此次请求加锁的客户端 和 给已存在的锁上锁的客户端为同一客户端
     * if (redis.call('hexists', KEYS[1], ARGV[2]) == 1)
     *      // 给 ARGV[2] 字段对应的数值加一，此数值代表可重入次数
     *      then redis.call('hincrby', KEYS[1], ARGV[2], 1);
     *      // 重新设定锁的过期时间
     *      redis.call('pexpire', KEYS[1], ARGV[1]);
     *      return nil;
     *  end;
     *
     *  // 锁存在，但 ARGV[2] 不同，意味着本次请求客户端和 给已存在的锁上锁客户端为 不同客户端
     *  // 只返回已存在锁的过期时间
     *  return redis.call('pttl', KEYS[1]);
     *
     * @return
     */
    public boolean getLock(Long userId,Integer expireTime){
        // 先获取锁，获取成功就继续，不成功(立即返回 或者 阻塞等待超时)
        RLock lock = redissonClient.getLock(String.valueOf(userId));
        try {
            return lock.tryLock(100, expireTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("getLock error,param:{}", userId, e);
            return false;
        }
    }


    /**
     * 锁过程的详情可见 {@link org.redisson.RedissonLock#unlockInnerAsync}
     * <br/>
     * 具体 lua 脚本如下:
     *
     * // 锁是存在 && 客户端标识和锁存储的标识匹配
     *"if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then
     *      // 不匹配则返回 nil
     *      return nil;
     * end;
     * // 匹配则可重入次数 -1
     * local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1);
     * if (counter > 0) then
     *      // 剩余可重如次数 >0 则返回 ttl
     *      redis.call('pexpire', KEYS[1], ARGV[2]);
     *      return 0;
     * else
     *      // 剩余可重如次数 = 0 则删除锁
     *      redis.call('del', KEYS[1]);
     *      redis.call('publish', KEYS[2], ARGV[1]);
     *      return 1;
     * end;
     * return nil;
     */
    public boolean releaseLock(Long userId) {
        RLock lock = redissonClient.getLock(String.valueOf(userId));
        if (lock.isLocked()) {
            lock.unlock();
            return true;
        }
        return false;
    }

}
