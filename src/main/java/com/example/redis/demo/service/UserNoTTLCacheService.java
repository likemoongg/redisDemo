package com.example.redis.demo.service;

import com.example.redis.demo.constant.CacheConstant;
import com.example.redis.demo.domain.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@CacheConfig(cacheNames = CacheConstant.USER_INFO_WITHOUT_TTL)
public class UserNoTTLCacheService {

    /**
     * 不考虑多线程，用这个模拟存储啦，方便 debug!
     */
    private UserInfo userInfo = new UserInfo();



    /**
     * 缓存用法，一般放在读接口
     *
     * 访问 http://localhost:8080/withoutttl/getUserInfo?id=1
     * 传入的参数为
     * {
     *    "userName": "lihaoyue",
     *    "userAge": 28,
     *    "userId": 1
     * }
     *
     * 则 redis-server 执行命令 "HGET" "uinfo_no_ttl" "\x03userInfo\xb1"
     * 如果命中缓存，则方法不执行。
     * 如果未命中缓存，则执行方法，并将返回值放到 redis 中，并执行 "HSET" "uinfo_no_ttl" "\x03userInfo\xb1" "\x01\x00com.example.redis.demo.domain.UserInf\xef\x018\x01\x02lihaoyu\xe5"
     */
    @Cacheable(key = "'userInfo'+#userId")
    public UserInfo getUserInfo(Long userId) {
        return userInfo;
    }


    /**
     * 缓存清理，一般放在写接口上
     *
     * 调用 http://localhost:8080/withoutttl/setUserService
     *
     * 则 redis-server 执行命令 "HDEL" "uinfo_no_ttl" "\x03userInfo\xb1"
     */
    @CacheEvict(key = "'userInfo' +#userInfo.userId")
    public void saveUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

}
