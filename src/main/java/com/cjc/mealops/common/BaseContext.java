package com.cjc.mealops.common;

public final class BaseContext {
    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_ROLE = new ThreadLocal<>();

    private BaseContext() {
    }

    public static void setCurrentId(Long id) {
        CURRENT_ID.set(id);
    }

    public static Long getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void setCurrentRole(String role) {
        CURRENT_ROLE.set(role);
    }

    public static String getCurrentRole() {
        return CURRENT_ROLE.get();
    }

    public static void clear() {
        CURRENT_ID.remove();
        CURRENT_ROLE.remove();
    }
}
