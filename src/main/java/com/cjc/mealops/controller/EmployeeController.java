package com.cjc.mealops.controller;

import com.cjc.mealops.common.R;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/employee")
public class EmployeeController {
    private final ApiInvokeSupport api;

    public EmployeeController(ApiInvokeSupport api) {
        this.api = api;
    }

    @PostMapping("/login")
    public R<Object> login(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("employeeService", "login", body));
    }

    @PostMapping("/logout")
    public R<Object> logout() {
        return R.success(api.invoke("employeeService", List.of("logout", "signOut")));
    }

    @PostMapping
    public R<Object> create(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("employeeService", List.of("create", "save", "add"), body));
    }

    @GetMapping("/page")
    public R<Object> page(@RequestParam Map<String, String> params) {
        return R.success(api.invoke("employeeService", List.of("page", "pageQuery"), api.query(params)));
    }

    @GetMapping("/{id}")
    public R<Object> getById(@PathVariable Long id) {
        return R.success(api.invoke("employeeService", List.of("getById", "detail"), id));
    }

    @PutMapping
    public R<Object> update(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("employeeService", List.of("update", "updateById"), body));
    }

    @RequestMapping(value = "/status/{status}", method = {RequestMethod.POST, RequestMethod.PUT})
    public R<Object> updateStatus(@PathVariable Integer status, @RequestParam Long id) {
        return R.success(api.invoke("employeeService", List.of("updateStatus", "status", "setStatus"), status, id));
    }
}
