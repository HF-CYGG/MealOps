package com.cjc.mealops.controller;

import com.cjc.mealops.common.AuthUtils;
import com.cjc.mealops.common.R;
import com.cjc.mealops.dto.DiningTableDTO;
import com.cjc.mealops.service.DiningTableService;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/diningTables")
public class DiningTableController {
    private final DiningTableService diningTableService;

    public DiningTableController(DiningTableService diningTableService) {
        this.diningTableService = diningTableService;
    }

    @GetMapping
    public R<Object> page(@RequestParam Map<String, String> params) {
        AuthUtils.requireEmployee();
        return R.success(diningTableService.page(Map.copyOf(params)));
    }

    @GetMapping("/page")
    public R<Object> pageAlias(@RequestParam Map<String, String> params) {
        return page(params);
    }

    @PostMapping
    public R<Object> create(@Valid @RequestBody DiningTableDTO dto) {
        AuthUtils.requireEmployee();
        return R.success(diningTableService.create(dto));
    }

    @PutMapping
    public R<Object> update(@Valid @RequestBody DiningTableDTO dto) {
        AuthUtils.requireEmployee();
        return R.success(diningTableService.update(dto));
    }

    @PutMapping("/status/{status}")
    public R<Object> status(@PathVariable String status, @RequestParam String ids) {
        AuthUtils.requireEmployee();
        diningTableService.updateStatus(status, parseIds(ids));
        return R.success(null);
    }

    private List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .filter(value -> !value.isBlank())
                .map(Long::parseLong)
                .toList();
    }
}
