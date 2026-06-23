package com.cjc.mealops.controller;

import com.cjc.mealops.common.AuthUtils;
import com.cjc.mealops.common.R;
import com.cjc.mealops.dto.DiningCartItemDTO;
import com.cjc.mealops.dto.DiningCartItemUpdateDTO;
import com.cjc.mealops.dto.DiningPartySizeDTO;
import com.cjc.mealops.dto.DiningSessionCreateDTO;
import com.cjc.mealops.service.DiningSessionService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/diningSessions")
public class DiningSessionController {
    private final DiningSessionService diningSessionService;

    public DiningSessionController(DiningSessionService diningSessionService) {
        this.diningSessionService = diningSessionService;
    }

    @PostMapping
    public R<Object> create(@Valid @RequestBody DiningSessionCreateDTO dto) {
        AuthUtils.requireUser();
        return R.success(diningSessionService.createSession(dto));
    }

    @GetMapping("/page")
    public R<Object> page(@RequestParam Map<String, String> params) {
        AuthUtils.requireEmployee();
        return R.success(diningSessionService.page(Map.copyOf(params)));
    }

    @GetMapping("/{sessionId}")
    public R<Object> detail(@PathVariable Long sessionId) {
        return R.success(diningSessionService.detail(sessionId));
    }

    @PostMapping("/{sessionId}/join")
    public R<Object> join(@PathVariable Long sessionId) {
        AuthUtils.requireUser();
        return R.success(diningSessionService.join(sessionId));
    }

    @PatchMapping("/{sessionId}/party-size")
    public R<Object> partySize(@PathVariable Long sessionId, @Valid @RequestBody DiningPartySizeDTO dto) {
        AuthUtils.requireUser();
        return R.success(diningSessionService.updatePartySize(sessionId, dto.getPartySize()));
    }

    @PostMapping("/{sessionId}/leave")
    public R<Object> leave(@PathVariable Long sessionId) {
        AuthUtils.requireUser();
        diningSessionService.leave(sessionId);
        return R.success(null);
    }

    @GetMapping("/{sessionId}/cart")
    public R<Object> cart(@PathVariable Long sessionId) {
        AuthUtils.requireUser();
        return R.success(diningSessionService.cart(sessionId));
    }

    @PostMapping("/{sessionId}/cart/items")
    public R<Object> addCartItem(@PathVariable Long sessionId, @Valid @RequestBody DiningCartItemDTO dto) {
        AuthUtils.requireUser();
        return R.success(diningSessionService.addCartItem(sessionId, dto));
    }

    @PatchMapping("/{sessionId}/cart/items/{itemId}")
    public R<Object> updateCartItem(@PathVariable Long sessionId,
                                    @PathVariable Long itemId,
                                    @Valid @RequestBody DiningCartItemUpdateDTO dto) {
        AuthUtils.requireUser();
        return R.success(diningSessionService.updateCartItem(sessionId, itemId, dto.getNumber()));
    }

    @DeleteMapping("/{sessionId}/cart/items/{itemId}")
    public R<Object> deleteCartItem(@PathVariable Long sessionId, @PathVariable Long itemId) {
        AuthUtils.requireUser();
        diningSessionService.deleteCartItem(sessionId, itemId);
        return R.success(null);
    }

    @DeleteMapping("/{sessionId}/cart")
    public R<Object> clearCart(@PathVariable Long sessionId) {
        AuthUtils.requireUser();
        diningSessionService.clearCart(sessionId);
        return R.success(null);
    }

    @PostMapping("/{sessionId}/orders")
    public R<Object> submitOrder(@PathVariable Long sessionId) {
        AuthUtils.requireUser();
        return R.success(diningSessionService.submitOrder(sessionId));
    }

    @GetMapping("/{sessionId}/ordered-items")
    public R<Object> orderedItems(@PathVariable Long sessionId) {
        AuthUtils.requireUser();
        return R.success(diningSessionService.orderedItems(sessionId));
    }

    @PostMapping("/{sessionId}/force-close")
    public R<Object> forceClose(@PathVariable Long sessionId) {
        AuthUtils.requireEmployee();
        diningSessionService.forceClose(sessionId);
        return R.success(null);
    }
}
