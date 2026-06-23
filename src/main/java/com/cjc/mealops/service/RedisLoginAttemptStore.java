package com.cjc.mealops.service;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisLoginAttemptStore implements LoginAttemptStore {
    private static final String FAIL_PREFIX = "login:fail:";
    private static final String LOCK_PREFIX = "login:lock:";
    private final StringRedisTemplate redisTemplate;
    private final InMemoryLoginAttemptStore fallback = new InMemoryLoginAttemptStore();

    public RedisLoginAttemptStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public int incrementFailures(String username, Duration ttl) {
        try {
            String key = FAIL_PREFIX + username;
            Long count = redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, ttl);
            return count == null ? 0 : count.intValue();
        } catch (RuntimeException ex) {
            return fallback.incrementFailures(username, ttl);
        }
    }

    @Override
    public boolean isLocked(String username) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(LOCK_PREFIX + username));
        } catch (RuntimeException ex) {
            return fallback.isLocked(username);
        }
    }

    @Override
    public void lock(String username, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(LOCK_PREFIX + username, "1", ttl);
        } catch (RuntimeException ex) {
            fallback.lock(username, ttl);
        }
    }

    @Override
    public void clear(String username) {
        try {
            redisTemplate.delete(FAIL_PREFIX + username);
            redisTemplate.delete(LOCK_PREFIX + username);
        } catch (RuntimeException ex) {
            fallback.clear(username);
        }
    }
}
