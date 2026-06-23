package com.cjc.mealops.controller;

import com.cjc.mealops.common.AuthUtils;
import com.cjc.mealops.common.R;
import com.cjc.mealops.service.PaymentService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders/{orderId}/payments/prepay")
    public R<Object> prepay(@PathVariable Long orderId) {
        AuthUtils.requireUser();
        return R.success(paymentService.prepay(orderId));
    }

    @GetMapping("/orders/{orderId}/payment")
    public R<Object> getByOrder(@PathVariable Long orderId) {
        AuthUtils.requireUser();
        return R.success(paymentService.getByOrderId(orderId));
    }

    @PostMapping("/payments/{paymentId}/confirm")
    public R<Object> confirm(@PathVariable Long paymentId) {
        AuthUtils.requireUser();
        return R.success(paymentService.confirm(paymentId));
    }

    @GetMapping("/payments/page")
    public R<Object> page(@RequestParam Map<String, String> params) {
        AuthUtils.requireEmployee();
        return R.success(paymentService.page(Map.copyOf(params)));
    }

    @GetMapping("/payments/{paymentId}")
    public R<Object> getById(@PathVariable Long paymentId) {
        AuthUtils.requireEmployee();
        return R.success(paymentService.getById(paymentId));
    }
}
