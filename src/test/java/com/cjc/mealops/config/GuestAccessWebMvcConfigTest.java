package com.cjc.mealops.config;

// 本测试文件用于验证后端拦截器白名单配置，确保游客可访问菜单列表接口，但下单接口仍受登录保护。

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cjc.mealops.interceptor.LoginInterceptor;
import com.cjc.mealops.util.JwtUtils;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

class GuestAccessWebMvcConfigTest {

    @Test
    void 白名单应放行游客访问三个列表接口() {
        List<String> excludePatterns = getExcludePatterns();

        assertTrue(excludePatterns.contains("/category/list"));
        assertTrue(excludePatterns.contains("/dish/list"));
        assertTrue(excludePatterns.contains("/setmeal/list"));
        assertTrue(excludePatterns.contains("/common/download"));
        assertTrue(excludePatterns.contains("/favicon.svg"));
    }

    @Test
    void 白名单不应放行下单和上传接口() {
        List<String> excludePatterns = getExcludePatterns();

        assertFalse(excludePatterns.contains("/order/submit"));
        assertFalse(excludePatterns.contains("/common/upload"));
    }

    @SuppressWarnings("unchecked")
    private List<String> getExcludePatterns() {
        // 这里直接读取 Spring 注册结果，避免测试依赖完整 Web 容器启动。
        WebMvcConfig config = new WebMvcConfig(new LoginInterceptor(new JwtUtils("test-secret", Duration.ofHours(1))));
        InterceptorRegistry registry = new InterceptorRegistry();
        config.addInterceptors(registry);

        List<InterceptorRegistration> registrations =
                (List<InterceptorRegistration>) ReflectionTestUtils.getField(registry, "registrations");
        InterceptorRegistration registration = registrations.get(0);
        return (List<String>) ReflectionTestUtils.getField(registration, "excludePatterns");
    }
}
