package com.example.redis.demo.config;

import com.example.redis.demo.constant.CacheConstant;
import com.example.redis.demo.service.CustomCacheKeyGenerator;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 此类主要用于管理 spring 注解形式 e.g:@Cacheable\@CacheEvict 等
 */
@Configuration
@EnableCaching(proxyTargetClass = true)
public class RedisManagerConfig extends CachingConfigurerSupport {

    RedissonClient redissonClient;

    RedisManagerConfig(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    @Bean
    public CacheManager cacheManager() {
        Map<String, CacheConfig> config = new HashMap<>();

        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setTTL(TimeUnit.HOURS.toSeconds(2));
        config.put(CacheConstant.USER_INFO, cacheConfig);

        cacheConfig = new CacheConfig();
        config.put(CacheConstant.USER_INFO_WITHOUT_TTL, cacheConfig);

        return new RedissonSpringCacheManager(redissonClient, config);
    }

    /**
     * 定义 key 的生成规则
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new CustomCacheKeyGenerator();
    }

}
