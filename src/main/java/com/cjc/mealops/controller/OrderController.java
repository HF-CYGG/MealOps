package com.cjc.mealops.controller;

import com.cjc.mealops.common.R;
import com.cjc.mealops.dto.OrdersSubmitDTO;
import com.cjc.mealops.service.OrderService;
import com.cjc.mealops.vo.OrderVO;
import com.cjc.mealops.vo.OrderSubmitVO;
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
@RequestMapping("/order")
public class OrderController {
    private final ApiInvokeSupport api;
    private final OrderService orderService;

    public OrderController(ApiInvokeSupport api, OrderService orderService) {
        this.api = api;
        this.orderService = orderService;
    }

    @PostMapping("/submit")
    public R<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO body) {
        return R.success(orderService.submit(body));
    }

    @GetMapping("/page")
    public R<Object> page(@RequestParam Map<String, String> params) {
        return R.success(api.invoke("orderService", List.of("pageQuery", "page"), api.query(params)));
    }

    @GetMapping("/history")
    public R<Object> history(@RequestParam Map<String, String> params) {
        return R.success(api.invoke("orderService", List.of("history", "userPage", "pageByCurrentUser"), api.query(params)));
    }

    @GetMapping("/userPage")
    public R<Object> userPage(@RequestParam Map<String, String> params) {
        return R.success(api.invoke("orderService", List.of("userPage", "history", "pageByCurrentUser"), api.query(params)));
    }

    @GetMapping("/{id}")
    public R<OrderVO> getById(@PathVariable Long id) {
        return R.success(orderService.detail(id));
    }

    @PutMapping("/cancel/{id}")
    public R<Object> cancel(@PathVariable Long id) {
        return R.success(api.invoke("orderService", List.of("cancel", "cancelById"), id, ""));
    }

    @PostMapping("/cancel")
    public R<Object> cancel(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("orderService", List.of("cancel", "cancelById"),
                Long.parseLong(String.valueOf(body.get("id"))), String.valueOf(body.getOrDefault("reason", ""))));
    }

    @PostMapping("/status")
    public R<Object> status(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("orderService", List.of("updateStatus", "status"),
                Long.parseLong(String.valueOf(body.get("id"))), Integer.parseInt(String.valueOf(body.get("status")))));
    }

    @PostMapping("/repetition/{id}")
    public R<Object> repetition(@PathVariable Long id) {
        return R.success(api.invoke("orderService", List.of("repetition", "repeat", "repeatOrder"), id));
    }

    @GetMapping("/reminder/{id}")
    public R<Object> reminder(@PathVariable Long id) {
        return R.success(api.invoke("orderService", List.of("reminder", "remind"), id));
    }

    @PutMapping("/confirm")
    public R<Object> confirm(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("orderService", List.of("confirm", "confirmOrder"),
                Long.parseLong(String.valueOf(body.get("id")))));
    }

    @PutMapping("/rejection")
    public R<Object> rejection(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("orderService", List.of("reject", "rejection"),
                Long.parseLong(String.valueOf(body.get("id"))), String.valueOf(body.getOrDefault("rejectionReason", ""))));
    }

    @PutMapping("/delivery/{id}")
    public R<Object> delivery(@PathVariable Long id) {
        return R.success(api.invoke("orderService", List.of("delivery", "deliver"), id));
    }

    @PutMapping("/complete/{id}")
    public R<Object> complete(@PathVariable Long id) {
        return R.success(api.invoke("orderService", List.of("complete", "finish"), id));
    }
}
