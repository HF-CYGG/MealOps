package com.cjc.mealops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.AddressBook;
import com.cjc.mealops.entity.Category;
import com.cjc.mealops.entity.Employee;
import com.cjc.mealops.entity.OperationLog;
import com.cjc.mealops.entity.Orders;
import com.cjc.mealops.entity.ShoppingCart;
import com.cjc.mealops.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ApiInvokeSupport {
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    public ApiInvokeSupport(ApplicationContext applicationContext, ObjectMapper objectMapper) {
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
    }

    public Object invoke(String beanName, String methodName, Object... args) {
        return invoke(beanName, List.of(methodName), args);
    }

    public Object invoke(String beanName, List<String> methodNames, Object... args) {
        Object service = findBean(beanName);
        List<Method> methods = new ArrayList<>();
        for (Method method : service.getClass().getMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && methodNames.contains(method.getName())) {
                methods.add(method);
            }
        }
        methods.sort(Comparator
                .comparingInt((Method method) -> methodNames.indexOf(method.getName()))
                .thenComparingInt(method -> method.getParameterTypes().length));

        for (Method method : methods) {
            Object[] converted = convertArguments(beanName, method, args);
            if (converted == null && args.length != 1) {
                converted = convertArguments(beanName, method, new Object[] {toPayload(args)});
            }
            if (converted == null) {
                continue;
            }
            try {
                return method.invoke(service, converted);
            } catch (ReflectiveOperationException ex) {
                Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new BusinessException(cause.getMessage());
            }
        }
        Object commonResult = tryCommonServiceMethod(service, methodNames, args);
        if (commonResult != Unconvertible.INSTANCE) {
            return commonResult;
        }
        throw new BusinessException("服务方法未实现: " + beanName + "." + methodNames);
    }

    public Map<String, Object> query(Map<String, String> params) {
        return new LinkedHashMap<>(params);
    }

    public List<Long> ids(String rawIds) {
        if (rawIds == null || rawIds.isBlank()) {
            return List.of();
        }
        String[] values = rawIds.split(",");
        List<Long> ids = new ArrayList<>(values.length);
        for (String value : values) {
            if (!value.isBlank()) {
                ids.add(Long.parseLong(value.trim()));
            }
        }
        return ids;
    }

    private Object findBean(String beanName) {
        if (applicationContext.containsBean(beanName)) {
            return applicationContext.getBean(beanName);
        }
        String className = "com.cjc.mealops.service." + Character.toUpperCase(beanName.charAt(0)) + beanName.substring(1);
        try {
            Class<?> serviceType = Class.forName(className);
            return applicationContext.getBean(serviceType);
        } catch (ClassNotFoundException ex) {
            throw new BusinessException("服务未实现: " + beanName);
        }
    }

    private Object[] convertArguments(String beanName, Method method, Object[] args) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != args.length) {
            return null;
        }
        Object[] converted = new Object[args.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> targetType = resolveGenericEntityType(beanName, method, parameterTypes[i], args[i]);
            Object value = convert(targetType, args[i]);
            if (value == Unconvertible.INSTANCE) {
                return null;
            }
            converted[i] = value;
        }
        return converted;
    }

    private Object tryCommonServiceMethod(Object service, List<String> methodNames, Object[] args) {
        if (!(service instanceof IService<?> serviceApi)) {
            return Unconvertible.INSTANCE;
        }
        if (args.length == 1 && args[0] instanceof Map<?, ?> params) {
            if (methodNames.contains("page")) {
                return serviceApi.page(new Page<>(longParam(params, "page", 1L), longParam(params, "pageSize", 10L)));
            }
            if (methodNames.contains("list")) {
                return serviceApi.list();
            }
        }
        return Unconvertible.INSTANCE;
    }

    private long longParam(Map<?, ?> params, String name, long defaultValue) {
        Object value = params.get(name);
        if (value == null) {
            return defaultValue;
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Class<?> resolveGenericEntityType(String beanName, Method method, Class<?> parameterType, Object value) {
        if (parameterType != Object.class || !(value instanceof Map<?, ?>)) {
            return parameterType;
        }
        if (!List.of("save", "update", "updateById").contains(method.getName())) {
            return parameterType;
        }
        return switch (beanName) {
            case "employeeService" -> Employee.class;
            case "categoryService" -> Category.class;
            case "userService" -> User.class;
            case "addressBookService" -> AddressBook.class;
            case "shoppingCartService" -> ShoppingCart.class;
            case "orderService" -> Orders.class;
            case "operationLogService" -> OperationLog.class;
            default -> parameterType;
        };
    }

    private Object convert(Class<?> targetType, Object value) {
        if (value == null) {
            return targetType.isPrimitive() ? Unconvertible.INSTANCE : null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }
        if (targetType == String.class) {
            return String.valueOf(value);
        }
        if (targetType == Long.class || targetType == Long.TYPE) {
            return Long.parseLong(String.valueOf(value));
        }
        if (targetType == Integer.class || targetType == Integer.TYPE) {
            return Integer.parseInt(String.valueOf(value));
        }
        if (targetType == BigDecimal.class) {
            return new BigDecimal(String.valueOf(value));
        }
        if (targetType == Boolean.class || targetType == Boolean.TYPE) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        if (Collection.class.isAssignableFrom(targetType) && value instanceof Collection<?>) {
            return value;
        }
        if (Map.class.isAssignableFrom(targetType) && value instanceof Map<?, ?>) {
            return value;
        }
        try {
            return objectMapper.convertValue(value, targetType);
        } catch (IllegalArgumentException ex) {
            return Unconvertible.INSTANCE;
        }
    }

    private Map<String, Object> toPayload(Object[] args) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            payload.put("arg" + i, args[i]);
        }
        return payload;
    }

    private enum Unconvertible {
        INSTANCE
    }
}
