package com.cjc.mealops.controller;

import jakarta.servlet.http.HttpServletResponse;
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
    public String forwardAdminRoute(HttpServletResponse response) {
        return forwardIndex(response);
    }

    @GetMapping({
            "/client",
            "/client/login",
            "/client/home",
            "/client/menu",
            "/client/order/submit",
            "/client/order/history"
    })
    public String forwardClientRoute(HttpServletResponse response) {
        return forwardIndex(response);
    }

    private String forwardIndex(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        return INDEX_FORWARD;
    }
}
