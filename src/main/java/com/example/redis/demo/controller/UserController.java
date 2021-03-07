package com.example.redis.demo.controller;

import com.example.redis.demo.domain.UserInfo;
import com.example.redis.demo.service.UserNoTTLCacheService;
import com.example.redis.demo.service.UserCacheService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/")
public class UserController {

    @Resource
    private UserCacheService userService;

    @Resource
    private UserNoTTLCacheService userNoCacheService;

    @PostMapping("/setUserService")
    public void setUserService(@RequestBody UserInfo userInfo) {
        userService.saveUserInfo(userInfo);
    }

    @GetMapping("/getUserInfo")
    public UserInfo getUserInfo(@RequestParam("id") Long userId) {
        return userService.getUserInfo(userId);
    }

    @PostMapping("/withoutttl/setUserService")
    public void setUserServiceWithoutTTL(@RequestBody UserInfo userInfo) {
        userNoCacheService.saveUserInfo(userInfo);
    }

    @GetMapping("/withoutttl/getUserInfo")
    public UserInfo getUserInfoWithoutTTL(@RequestParam("id") Long userId) {
        return userNoCacheService.getUserInfo(userId);
    }

}
