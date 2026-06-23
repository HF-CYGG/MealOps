package com.cjc.mealops.config;

import com.cjc.mealops.service.CategoryDeletionPolicy;
import com.cjc.mealops.service.LoginAttemptStore;
import com.cjc.mealops.service.LoginSecurityService;
import com.cjc.mealops.service.OrderCalculator;
import com.cjc.mealops.util.JwtUtils;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppBeansConfig {

    @Bean
    JwtUtils jwtUtils(
            @Value("${mealops.jwt.secret:mealops-local-secret-change-me}") String secret,
            @Value("${mealops.jwt.ttl-hours:2}") long ttlHours) {
        return new JwtUtils(secret, Duration.ofHours(ttlHours));
    }

    @Bean
    LoginSecurityService loginSecurityService(LoginAttemptStore store) {
        return new LoginSecurityService(store, 5, Duration.ofMinutes(10));
    }

    @Bean
    OrderCalculator orderCalculator() {
        return new OrderCalculator();
    }

    @Bean
    CategoryDeletionPolicy categoryDeletionPolicy() {
        return new CategoryDeletionPolicy();
    }
}
