package com.cjc.mealops.controller;

import com.cjc.mealops.common.R;
import java.sql.Connection;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 本文件用于提供应用健康检查接口。
 * 除了判断 Spring Boot 进程是否启动成功，还会同步检查数据库连接是否可用，
 * 避免出现“健康检查通过但业务接口因数据库不可用全部失败”的误判。
 */
@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<R<Map<String, String>>> health() {
        try (Connection connection = dataSource.getConnection()) {
            // 这里使用数据库连接有效性校验，确保健康状态能覆盖最关键的业务依赖。
            boolean databaseAvailable = connection.isValid(2);
            if (!databaseAvailable) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(R.error("数据库连接不可用"));
            }

            return ResponseEntity.ok(R.success(Map.of(
                    "status", "UP",
                    "service", "MealOps",
                    "database", "UP"
            )));
        } catch (Exception ex) {
            // 当数据库连接创建失败或校验失败时，直接把健康状态标记为不可用，便于快速排障。
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(R.error("数据库连接不可用"));
        }
    }
}
