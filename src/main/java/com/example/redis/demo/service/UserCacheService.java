package com.example.redis.demo.service;

import com.example.redis.demo.constant.CacheConstant;
import com.example.redis.demo.domain.UserInfo;
import com.example.redis.demo.service.redis.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class UserCacheService {

    @Resource
    private CacheService cacheService;

    /**
     * 不考虑多线程，用这个模拟存储啦，方便 debug!
     */
    private UserInfo userInfo = new UserInfo();

    public UserInfo getUserInfo(Long userId) {
        UserInfo cachedUserInfo = cacheService.get(getRedisKey(userId), UserInfo.class);
        if (cachedUserInfo == null) {
            cacheService.setWithTTL(getRedisKey(userId), this.userInfo, 5L);
        }
        return this.userInfo;
    }

    public void saveUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        cacheService.delete(getRedisKey(userInfo.getUserId()));
    }

    private String getRedisKey(Object key) {
        return CacheConstant.USER_INFO + key;
    }

}
