package com.example.redis.demo.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.Kryo5Codec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisClientConfig {

    @Bean("redissonClient")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.setCodec(new Kryo5Codec())
                .useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("likemoongg")
                .setTimeout(30000)
                .setConnectTimeout(1000)
                .setTimeout(1000)
                .setRetryAttempts(3)
                .setRetryInterval(1000)
                .setDatabase(0);

        return Redisson.create(config);
    }

}
