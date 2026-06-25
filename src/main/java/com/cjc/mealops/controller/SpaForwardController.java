package com.cjc.mealops.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {
    private static final String INDEX_FORWARD = "forward:/index.html";

    @GetMapping({
            "/",
            "/login",
            "/home",
            "/employee",
            "/category",
            "/dish",
            "/setmeal",
            "/order"
    })
    public String forwardAdminRoute() {
        return INDEX_FORWARD;
    }

    @GetMapping({
            "/client",
            "/client/login",
            "/client/home",
            "/client/menu",
            "/client/order/submit",
            "/client/order/history"
    })
    public String forwardClientRoute() {
        return INDEX_FORWARD;
    }
}
