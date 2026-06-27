package com.cjc.mealops.controller;

import com.cjc.mealops.common.AuthUtils;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.common.R;
import com.cjc.mealops.service.StatsService;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/hot-dishes")
    public R<Object> hotDishes(@RequestParam Map<String, String> params) {
        AuthUtils.requireEmployee();
        int limit = Integer.parseInt(params.getOrDefault("limit", "10"));
        LocalDateTime beginTime = parseDateTime(params.get("beginTime"), LocalDateTime.now().minusDays(30));
        LocalDateTime endTime = parseDateTime(params.get("endTime"), LocalDateTime.now());
        return R.success(statsService.hotDishes(beginTime, endTime, limit));
    }

    @GetMapping("/overview")
    public R<Object> overview() {
        AuthUtils.requireEmployee();
        return R.success(statsService.todayOverview());
    }

    private LocalDateTime parseDateTime(String value, LocalDateTime defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("Invalid statistics time range");
        }
    }
}
