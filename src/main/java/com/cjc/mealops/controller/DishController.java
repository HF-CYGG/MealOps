package com.cjc.mealops.controller;

import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.common.R;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dish")
public class DishController {
    private final ApiInvokeSupport api;

    public DishController(ApiInvokeSupport api) {
        this.api = api;
    }

    @PostMapping
    public R<Object> create(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("dishService", List.of("create", "saveWithFlavor", "save"), body));
    }

    @GetMapping("/page")
    public R<Object> page(@RequestParam Map<String, String> params) {
        return R.success(api.invoke("dishService", List.of("pageQuery", "page"), api.query(params)));
    }

    @GetMapping("/list")
    public R<Object> list(@RequestParam Map<String, String> params) {
        return R.success(api.invoke("dishService", List.of("list", "listWithFlavor"), api.query(params)));
    }

    @GetMapping("/{id}")
    public R<Object> getById(@PathVariable Long id) {
        return R.success(api.invoke("dishService", List.of("getWithFlavor", "getByIdWithFlavor", "getById", "detail"), id));
    }

    @PutMapping
    public R<Object> update(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("dishService", List.of("updateWithFlavor", "update", "updateById"), body));
    }

    @DeleteMapping
    public R<Object> delete(@RequestParam String ids) {
        return R.success(api.invoke("dishService", List.of("deleteBatch", "delete", "removeBatchByIds"), api.ids(ids)));
    }

    @RequestMapping(value = "/status/{status}", method = {RequestMethod.POST, RequestMethod.PUT})
    public R<Object> updateStatus(
            @PathVariable Integer status,
            @RequestParam(required = false) String ids,
            @RequestParam(required = false) String id
    ) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("Invalid dish status");
        }
        String rawIds = ids == null || ids.isBlank() ? id : ids;
        if (rawIds == null || rawIds.isBlank()) {
            throw new BusinessException("Dish id is required");
        }
        List<Long> dishIds = api.ids(rawIds);
        if (dishIds.isEmpty()) {
            throw new BusinessException("Dish id is required");
        }
        return R.success(api.invoke("dishService", List.of("updateStatus", "startOrStop", "status", "setStatus"), status, dishIds));
    }
}
