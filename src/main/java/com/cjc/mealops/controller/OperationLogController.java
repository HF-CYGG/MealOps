package com.cjc.mealops.controller;

import com.cjc.mealops.common.R;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
public class OperationLogController {
    private final ApiInvokeSupport api;

    public OperationLogController(ApiInvokeSupport api) {
        this.api = api;
    }

    @GetMapping("/page")
    public R<Object> page(@RequestParam Map<String, String> params) {
        return R.success(api.invoke("operationLogService", List.of("page", "pageQuery"), api.query(params)));
    }
}
