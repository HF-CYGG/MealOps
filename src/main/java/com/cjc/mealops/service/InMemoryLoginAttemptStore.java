package com.cjc.mealops.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLoginAttemptStore implements LoginAttemptStore {
    private final Map<String, Integer> failures = new ConcurrentHashMap<>();
    private final Map<String, Long> lockedUntil = new ConcurrentHashMap<>();

    @Override
    public int incrementFailures(String username, Duration ttl) {
        return failures.merge(username, 1, Integer::sum);
    }

    @Override
    public boolean isLocked(String username) {
        Long until = lockedUntil.get(username);
        if (until == null) {
            return false;
        }
        if (until <= System.currentTimeMillis()) {
            lockedUntil.remove(username);
            return false;
        }
        return true;
    }

    @Override
    public void lock(String username, Duration ttl) {
        lockedUntil.put(username, System.currentTimeMillis() + ttl.toMillis());
    }

    @Override
    public void clear(String username) {
        failures.remove(username);
        lockedUntil.remove(username);
    }

    public int failures(String username) {
        return failures.getOrDefault(username, 0);
    }
}
