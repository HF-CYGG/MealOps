package com.cjc.mealops.service;

import java.time.Duration;

public interface LoginAttemptStore {
    int incrementFailures(String username, Duration ttl);

    boolean isLocked(String username);

    void lock(String username, Duration ttl);

    void clear(String username);
}
