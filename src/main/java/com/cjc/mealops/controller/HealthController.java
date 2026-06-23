package com.cjc.mealops.controller;

import com.cjc.mealops.common.R;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public R<Map<String, String>> health() {
        return R.success(Map.of("status", "UP", "service", "MealOps"));
    }
}
