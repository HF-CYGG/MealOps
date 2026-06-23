package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class LoginSecurityServiceTest {

    @Test
    void locksAccountForTenMinutesAfterFiveFailuresAndClearsOnSuccess() {
        InMemoryLoginAttemptStore store = new InMemoryLoginAttemptStore();
        LoginSecurityService service = new LoginSecurityService(store, 5, Duration.ofMinutes(10));

        for (int i = 0; i < 4; i++) {
            service.recordFailure("admin");
            assertThat(service.isLocked("admin")).isFalse();
        }

        service.recordFailure("admin");
        assertThat(service.isLocked("admin")).isTrue();

        service.clear("admin");
        assertThat(service.isLocked("admin")).isFalse();
        assertThat(store.failures("admin")).isZero();
    }
}
