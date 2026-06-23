package com.cjc.mealops.service;

import java.time.Duration;

public class LoginSecurityService {
    private final LoginAttemptStore store;
    private final int maxFailures;
    private final Duration lockDuration;

    public LoginSecurityService(LoginAttemptStore store, int maxFailures, Duration lockDuration) {
        this.store = store;
        this.maxFailures = maxFailures;
        this.lockDuration = lockDuration;
    }

    public void recordFailure(String username) {
        int failures = store.incrementFailures(username, lockDuration);
        if (failures >= maxFailures) {
            store.lock(username, lockDuration);
        }
    }

    public boolean isLocked(String username) {
        return store.isLocked(username);
    }

    public void clear(String username) {
        store.clear(username);
    }
}
