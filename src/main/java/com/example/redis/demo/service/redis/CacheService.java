package com.example.redis.demo.service.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.redis.demo.constant.CacheConstant;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
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
    private RedisTemplate<String, ?> redisTemplate;

    public boolean set(String key, final Object value) {
        //设置 值
        return redisTemplate.execute(
                (RedisCallback<Boolean>) connection -> {
                    String keyForRedis = resetKey(key);
                    RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    connection.set(serializer.serialize(keyForRedis), serializer.serialize(JSON.toJSONString(value)));
                    return true;
                }
        );
    }


    public boolean setWithTTL(String key, Object value,Long expireTime) {
        //设置 值
        boolean isSetSuccess = redisTemplate.execute(
                (RedisCallback<Boolean>) connection -> {
                    String keyForRedis = resetKey(key);
                    RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    connection.set(serializer.serialize(keyForRedis), serializer.serialize(JSON.toJSONString(value)), Expiration.seconds(expireTime), RedisStringCommands.SetOption.upsert());
                    return true;
                }
        );
        return isSetSuccess;
    }

    public <T> T get(String key, Class<T> clazz) {
        String result = redisTemplate.execute((RedisCallback<String>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            byte[] value = connection.get(serializer.serialize(resetKey(key)));
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
                    String keyForRedis = resetKey(key);
                    RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    connection.del(serializer.serialize(keyForRedis));
                    return true;
                }
        );
    }

    private String resetKey(String key) {
        return CacheConstant.USER_INFO + key;
    }
}
