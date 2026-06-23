package com.cjc.mealops.interceptor;

import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;

    public LoginInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException("未登录");
        }
        JwtUtils.JwtPayload payload = jwtUtils.parse(authorization.substring(BEARER_PREFIX.length()).trim());
        BaseContext.setCurrentId(payload.userId());
        BaseContext.setCurrentRole(payload.role());
        request.setAttribute("currentUserId", payload.userId());
        request.setAttribute("currentRole", payload.role());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.clear();
    }
}
