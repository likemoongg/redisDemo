package com.example.redis.demo.service;

import com.example.redis.demo.domain.UserInfo;
import com.example.redis.demo.service.redis.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.example.redis.demo.constant.CacheConstant.USER_INFO;

@Service
@Slf4j
@CacheConfig(cacheNames = USER_INFO)
public class UserCacheService {

    @Resource
    private CacheService cacheService;

    /**
     * 不考虑多线程，用这个模拟存储啦，方便 debug!
     */
    private UserInfo userInfo = new UserInfo();

    public UserInfo getUserInfo(Long userId) {
        UserInfo cachedUserInfo = cacheService.get(String.valueOf(userId), UserInfo.class);
        if (cachedUserInfo == null) {
            cacheService.setWithTTL(String.valueOf(userId), this.userInfo, 5L);
        }
        return this.userInfo;
    }

    public void saveUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        cacheService.delete(String.valueOf(userInfo.getUserId()));
    }

}
