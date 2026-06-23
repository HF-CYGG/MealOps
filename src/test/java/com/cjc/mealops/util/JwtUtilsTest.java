package com.cjc.mealops.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtUtilsTest {

    @Test
    void generatedTokenCanBeParsedBackToSubjectAndRole() {
        JwtUtils jwtUtils = new JwtUtils("mealops-test-secret", Duration.ofHours(2));

        String token = jwtUtils.generate(1001L, "employee");

        JwtUtils.JwtPayload payload = jwtUtils.parse(token);
        assertThat(payload.userId()).isEqualTo(1001L);
        assertThat(payload.role()).isEqualTo("employee");
        assertThat(payload.expiresAt()).isGreaterThan(System.currentTimeMillis());
    }
}
