package com.cjc.mealops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableCaching
@EnableScheduling
@MapperScan("com.cjc.mealops.mapper")
@SpringBootApplication
@EnableTransactionManagement
public class MealopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MealopsApplication.class, args);
    }
}
