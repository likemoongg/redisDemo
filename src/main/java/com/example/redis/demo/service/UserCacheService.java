package com.example.redis.demo.service;

import com.example.redis.demo.domain.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.example.redis.demo.constant.CacheConstant.USER_INFO;

@Service
@Slf4j
@CacheConfig(cacheNames = USER_INFO)
public class UserCacheService {

    @Resource
    private LockHelper lockHelper;

    /**
     * 不考虑多线程，用这个模拟存储啦，方便 debug!
     */
    private UserInfo userInfo = new UserInfo();


    /**
     * 缓存用法，一般放在读接口
     * 访问 http://localhost:8080/getUserInfo?id=1
     *
     * 首先从 redis 中取数据，底层是利用的 {@link org.redisson.RedissonMapCache#getOperationAsync} 实现的
     *      [0 lua] "hget" "uinfo" "\x03userInfo\xb1"
     *      [0 lua] "zscore" "redisson__timeout__set:{uinfo}" "\x03userInfo\xb1"
            [0 lua] "hget" "{uinfo}:redisson_options" "max-size"
     *
     * 如果未命中缓存，则会执行内部方法，并返回值储存到 redis 中，存储部分利用的 {@link org.redisson.RedissonMapCache#fastPutOperationAsync} 实现的，利用 hset 保存数据，过期时间用 zset 维护。
     *      最终执行的 redis 命令
     *      [0 lua] "hget" "uinfo" "\x03userInfo\xb1"
     *      [0 lua] "zscore" "redisson__timeout__set:{uinfo}" "\x03userInfo\xb1"
     *      [0 lua] "hget" "{uinfo}:redisson_options" "max-size"
     */
    @Cacheable(key = "'userInfo'+#userId")
    public UserInfo getUserInfo(Long userId) {
        return userInfo;
    }


    /**
     * 缓存清理，一般放在写接口上
     *
     * 访问 http://localhost:8080/setUserService
     * 传入的参数为
     * {
     *    "userName": "lihaoyue",
     *    "userAge": 28,
     *    "userId": 1
     * }
     *
     * 底层是利用的 {@link org.redisson.RedissonMapCache#fastRemoveOperationAsync} 实现的
     *
     * KEYS[0]  6                                              //
     * KEYS[1]  uinfo                                          //
     * KEYS[2]  redisson__timeout__set:{uinfo}                 //
     * KEYS[3]  redisson__idle__set:{uinfo}                    //
     * KEYS[4]  redisson_map_cache_removed:{uinfo}             //
     * KEYS[5]  redisson__map_cache__last_access__set:{uinfo}  //
     * KEYS[6]  {uinfo}:redisson_options                       //
     * KEYS[7]  \x03userInfo\xb1                               // 要存储的值
     *
     *
     * "HSET"
     * "EVAL" "local maxSize = tonumber(redis.call('hget', KEYS[6], 'max-size'));
     *  if maxSize ~= nil and maxSize ~= 0 then
     *     redis.call('zrem', KEYS[5], unpack(ARGV));
     *  end;
     *  redis.call('zrem', KEYS[3], unpack(ARGV));
     *  redis.call('zrem', KEYS[2], unpack(ARGV));
     *  for i, key in ipairs(ARGV) do
     *     local v = redis.call('hget', KEYS[1], key);
     *     if v ~= false then local t, val = struct.unpack('dLc0', v);
     *       local msg = struct.pack('Lc0Lc0', string.len(key), key, string.len(val), val);
     *       redis.call('publish', KEYS[4], msg);
     *     end;
     *  end;
     *  return redis.call('hdel', KEYS[1], unpack(ARGV));"
     *
     * "6" "uinfo" "redisson__timeout__set:{uinfo}" "redisson__idle__set:{uinfo}" "redisson_map_cache_removed:{uinfo}" "redisson__map_cache__last_access__set:{uinfo}" "{uinfo}:redisson_options" "\x03userInfo\xb1"
     *
     *
     * [0 lua] "hget" "{uinfo}:redisson_options" "max-size"
     * [0 lua] "zrem" "redisson__idle__set:{uinfo}" "\x03userInfo\xb1"
     * [0 lua] "zrem" "redisson__timeout__set:{uinfo}" "\x03userInfo\xb1"
     * [0 lua] "hget" "uinfo" "\x03userInfo\xb1"
     * [0 lua] "hdel" "uinfo" "\x03userInfo\xb1"
     */
    @CacheEvict(key = "'userInfo' +#userInfo.userId")
    public void saveUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String doSomeThingWithLock(Long userId) {
        // 先获取锁，获取成功就继续，获取不成功则返回

        if (lockHelper.getLock(userId,5 * 1000)) {
            String str = "获取到了锁，占用 5 秒钟，开始正常做事";
            log.info("str");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
            lockHelper.releaseLock(userId);
            return str;
        } else {
            String str = "获取失败，啥也不做";
            log.info("str");
            return str;
        }


    }

}
