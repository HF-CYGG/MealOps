package com.cjc.mealops.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.cjc.mealops.common.R;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Test
    void healthReportsUpWhenDatabaseConnectionIsAvailable() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);

        HealthController controller = new HealthController(dataSource);

        ResponseEntity<R<Map<String, String>>> response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(1);
        assertThat(response.getBody().getData()).containsEntry("status", "UP");
        assertThat(response.getBody().getData()).containsEntry("database", "UP");
    }

    @Test
    void healthReportsServiceUnavailableWhenDatabaseConnectionCannotBeObtained() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        HealthController controller = new HealthController(dataSource);

        ResponseEntity<R<Map<String, String>>> response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(0);
        assertThat(response.getBody().getMsg()).contains("数据库");
    }
}
