package com.cjc.mealops.config;

// 本文件用于在 Spring Boot 启动完成后输出后端访问入口与联调提示，减少本地启动后的查找成本。

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupHintRunner implements ApplicationRunner {

    // 固定使用默认本地开发地址输出提示，便于和 README、前端开发说明保持一致。
    private static final String LOCAL_BASE_URL = "http://localhost:8080";

    @Override
    public void run(ApplicationArguments args) {
        // 这里统一打印后端健康检查、Swagger 与内置静态联调页入口，帮助开发者在启动后直接访问。
        System.out.println();
        System.out.println("MealOps 后端服务已启动：");
        System.out.println("- 健康检查: " + LOCAL_BASE_URL + "/health");
        System.out.println("- Swagger UI: " + LOCAL_BASE_URL + "/swagger-ui.html");
        System.out.println("- 管理端静态联调页: " + LOCAL_BASE_URL + "/static/admin.html");
        System.out.println("- 用户端静态联调页: " + LOCAL_BASE_URL + "/static/user.html");
        System.out.println("- 若需调试 Vue 前端，请在 frontend 目录执行 npm run dev");
        System.out.println();
    }
}