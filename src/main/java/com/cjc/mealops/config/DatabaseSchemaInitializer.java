package com.cjc.mealops.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
@Order(0)
public class DatabaseSchemaInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);
    private static final List<String> REQUIRED_TABLES = List.of(
            "employee",
            "user",
            "category",
            "dish",
            "dish_flavor",
            "setmeal",
            "setmeal_dish",
            "shopping_cart",
            "address_book",
            "dining_table",
            "dining_session",
            "dining_session_member",
            "dining_cart_item",
            "orders",
            "order_detail",
            "payment_order",
            "operation_log"
    );

    private final DataSource dataSource;
    private final boolean enabled;
    private final boolean seedDataEnabled;

    public DatabaseSchemaInitializer(DataSource dataSource,
                                     @Value("${mealops.database-initializer.enabled:true}") boolean enabled,
                                     @Value("${mealops.database-initializer.seed-data-enabled:true}") boolean seedDataEnabled) {
        this.dataSource = dataSource;
        this.enabled = enabled;
        this.seedDataEnabled = seedDataEnabled;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!enabled) {
            log.info("Database schema initializer is disabled");
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            if (allRequiredTablesExist(connection)) {
                log.info("Database schema already exists, skipping initialization");
                return;
            }

            log.info("Database schema is missing, initializing MealOps tables");
            executeScript(connection, new ClassPathResource("db/schema.sql"), true);
            if (seedDataEnabled) {
                executeScript(connection, new ClassPathResource("db/data.sql"), false);
            }
            log.info("Database schema initialization completed");
        }
    }

    private boolean allRequiredTablesExist(Connection connection) throws SQLException {
        for (String table : REQUIRED_TABLES) {
            if (!tableExists(connection, table)) {
                log.info("Required table '{}' is missing", table);
                return false;
            }
        }
        return true;
    }

    static List<String> requiredTables() {
        return REQUIRED_TABLES;
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        String catalog = connection.getCatalog();
        try (ResultSet tables = connection.getMetaData().getTables(catalog, null, tableName, new String[]{"TABLE"})) {
            return tables.next();
        }
    }

    private void executeScript(Connection connection, Resource resource, boolean schemaScript) throws IOException {
        String script = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        String sanitized = schemaScript ? sanitizeSchemaScript(script) : sanitizeDataScript(script);
        ByteArrayResource sanitizedResource = new ByteArrayResource(
                sanitized.getBytes(StandardCharsets.UTF_8),
                "sanitized " + resource.getDescription()
        );
        ScriptUtils.executeSqlScript(connection, new EncodedResource(sanitizedResource, StandardCharsets.UTF_8));
    }

    static String sanitizeSchemaScript(String script) {
        return removeDatabaseSelection(script)
                .replaceAll("(?im)^\\s*DROP\\s+TABLE\\s+IF\\s+EXISTS\\s+[^;]+;\\s*", "")
                .replaceAll("(?i)CREATE\\s+TABLE\\s+(?!IF\\s+NOT\\s+EXISTS)", "CREATE TABLE IF NOT EXISTS ");
    }

    static String sanitizeDataScript(String script) {
        return removeDatabaseSelection(script)
                .replaceAll("(?i)INSERT\\s+INTO\\s+", "INSERT IGNORE INTO ");
    }

    private static String removeDatabaseSelection(String script) {
        return script
                .replaceAll("(?is)CREATE\\s+DATABASE\\s+IF\\s+NOT\\s+EXISTS\\s+[^;]+;\\s*", "")
                .replaceAll("(?im)^\\s*SET\\s+NAMES\\s+[^;]+;\\s*", "")
                .replaceAll("(?im)^\\s*USE\\s+[^;]+;\\s*", "");
    }
}
