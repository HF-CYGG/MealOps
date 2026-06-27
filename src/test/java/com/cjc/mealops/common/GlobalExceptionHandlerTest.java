package com.cjc.mealops.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

    @Test
    void missingStaticResourceReturnsNotFoundInsteadOfInternalServerError() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        R<Void> response = handler.resourceNotFound(new NoResourceFoundException(HttpMethod.GET, "favicon.ico"));

        assertThat(response.getCode()).isZero();
        assertThat(response.getMsg()).isEqualTo("资源不存在");

        Method method = GlobalExceptionHandler.class.getDeclaredMethod(
                "resourceNotFound",
                NoResourceFoundException.class
        );
        ResponseStatus status = method.getAnnotation(ResponseStatus.class);
        assertThat(status.value()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void dataIntegrityViolationReturnsBadRequestInsteadOfInternalServerError() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        R<Void> response = handler.dataIntegrity(new DataIntegrityViolationException("foreign key violation"));

        assertThat(response.getCode()).isZero();
        assertThat(response.getMsg()).isEqualTo("数据被其他记录引用，不能直接删除或修改");

        Method method = GlobalExceptionHandler.class.getDeclaredMethod(
                "dataIntegrity",
                DataIntegrityViolationException.class
        );
        ResponseStatus status = method.getAnnotation(ResponseStatus.class);
        assertThat(status.value()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
