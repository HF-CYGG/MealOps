package com.cjc.mealops.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ContainerRuntimeConfigTest {
    @Test
    void dockerImageDefinesContainerAwareJvmAndUtf8Defaults() throws Exception {
        String dockerfile = Files.readString(Path.of("Dockerfile"), StandardCharsets.UTF_8);

        assertThat(dockerfile).contains("JAVA_TOOL_OPTIONS");
        assertThat(dockerfile).contains("-XX:MaxRAMPercentage=");
        assertThat(dockerfile).contains("-Dfile.encoding=UTF-8");
        assertThat(dockerfile).contains("LANG=C.UTF-8");
        assertThat(dockerfile).contains("apk add --no-cache su-exec");
        assertThat(dockerfile).contains("exec su-exec mealops java -jar /app/app.jar");
        assertThat(dockerfile).doesNotContain("exec su mealops");
    }

    @Test
    void productionLoggingDoesNotDefaultMealopsToDebug() throws Exception {
        String applicationYaml = Files.readString(Path.of("src/main/resources/application.yml"), StandardCharsets.UTF_8);
        String logback = Files.readString(Path.of("src/main/resources/logback-spring.xml"), StandardCharsets.UTF_8);

        assertThat(applicationYaml).doesNotContain("com.cjc.mealops: DEBUG");
        assertThat(logback).doesNotContain("name=\"com.cjc.mealops\" level=\"DEBUG\"");
        assertThat(logback).contains("${MEALOPS_LOG_LEVEL:-INFO}");
    }
}
