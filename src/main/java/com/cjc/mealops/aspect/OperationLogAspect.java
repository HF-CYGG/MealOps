package com.cjc.mealops.aspect;

import com.cjc.mealops.common.BaseContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Aspect
@Component
public class OperationLogAspect {
    private static final int MAX_PARAM_LENGTH = 2000;
    private static final int MAX_EXCEPTION_LENGTH = 300;

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    public OperationLogAspect(ApplicationContext applicationContext, ObjectMapper objectMapper) {
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController) && within(com.cjc.mealops.controller..*) && !within(com.cjc.mealops.controller.HealthController)")
    public Object recordOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Throwable error = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            saveLog(joinPoint, System.currentTimeMillis() - start, error);
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long costTime, Throwable error) {
        try {
            Object service = findOperationLogService();
            if (service == null) {
                return;
            }
            Map<String, Object> payload = buildPayload(joinPoint, costTime, error);
            invokeSave(service, payload);
        } catch (Exception ignored) {
            // Operation logging must never change the API response.
        }
    }

    private Map<String, Object> buildPayload(ProceedingJoinPoint joinPoint, long costTime, Throwable error) {
        HttpServletRequest request = currentRequest();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("operationUser", BaseContext.getCurrentId());
        payload.put("operationType", request == null ? null : request.getMethod());
        payload.put("operationMethod", request == null ? null : request.getRequestURI());
        payload.put("operationParams", serializeArgs(joinPoint.getArgs()));
        payload.put("operationResult", error == null ? "SUCCESS" : summarize(error));
        payload.put("operationTime", LocalDateTime.now());
        payload.put("duration", costTime);
        payload.put("ip", request == null ? null : request.getRemoteAddr());
        return payload;
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String serializeArgs(Object[] args) {
        List<Object> values = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null
                    || arg instanceof ServletRequest
                    || arg instanceof ServletResponse
                    || arg instanceof MultipartFile) {
                continue;
            }
            values.add(arg);
        }
        try {
            return truncate(objectMapper.writeValueAsString(values), MAX_PARAM_LENGTH);
        } catch (Exception ex) {
            return truncate(values.toString(), MAX_PARAM_LENGTH);
        }
    }

    private String summarize(Throwable error) {
        if (error == null) {
            return null;
        }
        String message = error.getMessage() == null ? "" : ": " + error.getMessage();
        return truncate(error.getClass().getSimpleName() + message, MAX_EXCEPTION_LENGTH);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private Object findOperationLogService() {
        if (applicationContext.containsBean("operationLogService")) {
            return applicationContext.getBean("operationLogService");
        }
        try {
            Class<?> serviceType = Class.forName("com.cjc.mealops.service.OperationLogService");
            return applicationContext.getBean(serviceType);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    private void invokeSave(Object service, Map<String, Object> payload) throws ReflectiveOperationException {
        for (String methodName : List.of("save", "record", "create", "saveLog")) {
            for (Method method : service.getClass().getMethods()) {
                if (!Modifier.isPublic(method.getModifiers())
                        || !methodName.equals(method.getName())
                        || method.getParameterCount() != 1) {
                    continue;
                }
                Object argument = convertPayload(method.getParameterTypes()[0], payload);
                if (argument == null) {
                    continue;
                }
                method.invoke(service, argument);
                return;
            }
        }
    }

    private Object convertPayload(Class<?> targetType, Map<String, Object> payload) {
        if (Map.class.isAssignableFrom(targetType)) {
            return payload;
        }
        try {
            return objectMapper.convertValue(payload, targetType);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
