package com.cjc.mealops.common;

public final class AuthUtils {
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";
    public static final String ROLE_USER = "USER";

    private AuthUtils() {
    }

    public static void requireEmployee() {
        requireRole(ROLE_EMPLOYEE);
    }

    public static void requireUser() {
        requireRole(ROLE_USER);
    }

    public static boolean isEmployee() {
        return ROLE_EMPLOYEE.equals(BaseContext.getCurrentRole());
    }

    private static void requireRole(String role) {
        if (!role.equals(BaseContext.getCurrentRole())) {
            throw new BusinessException("Permission denied");
        }
    }
}
