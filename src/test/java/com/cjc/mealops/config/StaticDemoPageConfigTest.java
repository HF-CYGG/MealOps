package com.cjc.mealops.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class StaticDemoPageConfigTest {
    @Test
    void staticAdminPageUsesConfiguredDefaultPassword() throws Exception {
        String applicationYaml = Files.readString(Path.of("src/main/resources/application.yml"), StandardCharsets.UTF_8);
        String adminHtml = Files.readString(Path.of("src/main/resources/static/admin.html"), StandardCharsets.UTF_8);

        Matcher configured = Pattern.compile("MEALOPS_ADMIN_PASSWORD:([^}]+)}").matcher(applicationYaml);
        Matcher prefilled = Pattern.compile("id=\"adminPassword\"[^>]*value=\"([^\"]+)\"").matcher(adminHtml);

        assertThat(configured.find()).isTrue();
        assertThat(prefilled.find()).isTrue();
        assertThat(prefilled.group(1)).isEqualTo(configured.group(1));
    }
}
