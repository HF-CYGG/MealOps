package com.cjc.mealops.controller;

import com.cjc.mealops.common.R;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    private final ApiInvokeSupport api;

    public UserController(ApiInvokeSupport api) {
        this.api = api;
    }

    @PostMapping("/login")
    public R<Object> login(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("userService", "login", body));
    }

    @PostMapping("/logout")
    public R<Object> logout() {
        return R.success(api.invoke("userService", List.of("logout", "signOut")));
    }

    @GetMapping("/{id}")
    public R<Object> getById(@PathVariable Long id) {
        return R.success(api.invoke("userService", List.of("getById", "detail"), id));
    }
}
