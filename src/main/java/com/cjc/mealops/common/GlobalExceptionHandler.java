package com.cjc.mealops.common;

import jakarta.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> business(BusinessException ex) {
        return R.error(ex.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> validation(Exception ex) {
        return R.error("参数错误：" + ex.getMessage());
    }

    @ExceptionHandler({DuplicateKeyException.class, SQLIntegrityConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> duplicate(Exception ex) {
        return R.error("数据已存在或违反唯一约束");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> unexpected(Exception ex) {
        return R.error("系统异常：" + ex.getMessage());
    }
}
