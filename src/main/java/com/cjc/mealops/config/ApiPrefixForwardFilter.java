package com.cjc.mealops.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiPrefixForwardFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        String path = requestUri.substring(contextPath.length());

        if (path.equals("/api")) {
            request.getRequestDispatcher("/").forward(request, response);
            return;
        }
        if (path.startsWith("/api/")) {
            request.getRequestDispatcher(path.substring("/api".length())).forward(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
