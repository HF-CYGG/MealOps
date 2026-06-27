package com.cjc.mealops.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class DatabaseSchemaInitializerTest {

    @Test
    void schemaScriptIsSanitizedForApplicationStartup() {
        String script = """
                CREATE DATABASE IF NOT EXISTS reggie DEFAULT CHARACTER SET utf8mb4;
                SET NAMES utf8mb4;
                USE reggie;
                DROP TABLE IF EXISTS employee;
                CREATE TABLE employee (
                  id BIGINT NOT NULL AUTO_INCREMENT,
                  PRIMARY KEY (id)
                ) ENGINE=InnoDB;
                """;

        String sanitized = DatabaseSchemaInitializer.sanitizeSchemaScript(script);

        assertThat(sanitized).doesNotContain("CREATE DATABASE");
        assertThat(sanitized).doesNotContain("SET NAMES");
        assertThat(sanitized).doesNotContain("USE reggie");
        assertThat(sanitized).doesNotContain("DROP TABLE");
        assertThat(sanitized).contains("CREATE TABLE IF NOT EXISTS employee");
    }

    @Test
    void dataScriptIsSanitizedForRepeatedStartup() {
        String script = """
                SET NAMES utf8mb4;
                USE reggie;
                INSERT INTO employee (id, username) VALUES (1, 'admin');
                """;

        String sanitized = DatabaseSchemaInitializer.sanitizeDataScript(script);

        assertThat(sanitized).doesNotContain("SET NAMES");
        assertThat(sanitized).doesNotContain("USE reggie");
        assertThat(sanitized).contains("INSERT IGNORE INTO employee");
    }

    @Test
    void requiredTablesCoverEveryTableCreatedBySchema() throws Exception {
        String schema = Files.readString(Path.of("sql/schema.sql"), StandardCharsets.UTF_8);
        Pattern createTable = Pattern.compile("(?im)^\\s*CREATE\\s+TABLE\\s+`?([a-zA-Z0-9_]+)`?\\s*\\(");
        java.util.List<String> schemaTables = createTable.matcher(schema)
                .results()
                .map(match -> match.group(1))
                .toList();

        assertThat(DatabaseSchemaInitializer.requiredTables())
                .containsExactlyInAnyOrderElementsOf(schemaTables);
    }
}
