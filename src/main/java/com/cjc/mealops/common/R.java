package com.cjc.mealops.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> R<T> success(T data) {
        return new R<>(1, "success", data);
    }

    public static <T> R<T> success(String msg, T data) {
        return new R<>(1, msg, data);
    }

    public static <T> R<T> error(String msg) {
        return new R<>(0, msg, null);
    }
}
