package com.cjc.mealops.controller;

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
@RequestMapping("/setmeal")
public class SetmealController {
    private final ApiInvokeSupport api;

    public SetmealController(ApiInvokeSupport api) {
        this.api = api;
    }

    @PostMapping
    public R<Object> create(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("setmealService", List.of("create", "saveWithDish", "save"), body));
    }

    @GetMapping("/page")
    public R<Object> page(@RequestParam Map<String, String> params) {
        return R.success(api.invoke("setmealService", List.of("pageQuery", "page"), api.query(params)));
    }

    @GetMapping("/list")
    public R<Object> list(@RequestParam Map<String, String> params) {
        return R.success(api.invoke("setmealService", List.of("list", "listWithDish"), api.query(params)));
    }

    @GetMapping("/{id}")
    public R<Object> getById(@PathVariable Long id) {
        return R.success(api.invoke("setmealService", List.of("getWithDish", "getByIdWithDish", "getById", "detail"), id));
    }

    @PutMapping
    public R<Object> update(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("setmealService", List.of("updateWithDish", "update", "updateById"), body));
    }

    @DeleteMapping
    public R<Object> delete(@RequestParam String ids) {
        return R.success(api.invoke("setmealService", List.of("deleteBatch", "delete", "removeBatchByIds"), api.ids(ids)));
    }

    @RequestMapping(value = "/status/{status}", method = {RequestMethod.POST, RequestMethod.PUT})
    public R<Object> updateStatus(@PathVariable Integer status, @RequestParam String ids) {
        return R.success(api.invoke("setmealService", List.of("updateStatus", "startOrStop", "status", "setStatus"), status, api.ids(ids)));
    }
}
