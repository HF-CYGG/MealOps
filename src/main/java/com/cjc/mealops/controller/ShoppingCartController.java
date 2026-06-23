package com.cjc.mealops.controller;

import com.cjc.mealops.common.R;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    private final ApiInvokeSupport api;

    public ShoppingCartController(ApiInvokeSupport api) {
        this.api = api;
    }

    @GetMapping("/list")
    public R<Object> list() {
        return R.success(api.invoke("shoppingCartService", List.of("listCurrentUserCart", "listCurrentUser", "list")));
    }

    @PostMapping("/add")
    public R<Object> add(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("shoppingCartService", List.of("add", "create"), body));
    }

    @PostMapping("/sub")
    public R<Object> sub(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("shoppingCartService", List.of("sub", "subtract", "decrease"), body));
    }

    @DeleteMapping("/clean")
    public R<Object> clean() {
        return R.success(api.invoke("shoppingCartService", List.of("clean", "clear")));
    }
}
