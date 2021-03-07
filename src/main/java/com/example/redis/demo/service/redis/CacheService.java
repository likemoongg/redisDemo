package com.example.redis.demo.service.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Service
public class CacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public boolean set(String key, final Object value) {
        //设置 值
        return redisTemplate.execute(
                (RedisCallback<Boolean>) connection -> {
                    RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    connection.set(serializer.serialize(key), serializer.serialize(JSON.toJSONString(value)));
                    return true;
                }
        );
    }


    public boolean setWithTTL(String key, Object value,Long expireTime) {
        //设置 值
        boolean isSetSuccess = redisTemplate.execute(
                (RedisCallback<Boolean>) connection -> {
                    RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    connection.set(serializer.serialize(key), serializer.serialize(JSON.toJSONString(value)), Expiration.seconds(expireTime), RedisStringCommands.SetOption.upsert());
                    return true;
                }
        );
        return isSetSuccess;
    }

    public <T> T get(String key, Class<T> clazz) {
        String result = redisTemplate.execute((RedisCallback<String>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            byte[] value = connection.get(serializer.serialize(key));
            return serializer.deserialize(value);
        });
        if (StringUtils.hasText(key)) {
            return JSON.parseObject(result, clazz);
        }
        return null;
    }

    public void delete(String key) {
        redisTemplate.execute(
                (RedisCallback<Boolean>) connection -> {
                    RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    connection.del(serializer.serialize(key));
                    return true;
                }
        );
    }

}
