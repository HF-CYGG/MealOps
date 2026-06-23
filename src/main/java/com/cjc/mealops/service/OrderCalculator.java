package com.cjc.mealops.service;

import com.cjc.mealops.dto.CartItem;
import java.math.BigDecimal;
import java.util.List;

public class OrderCalculator {

    public BigDecimal total(List<CartItem> items) {
        return items.stream()
                .map(item -> item.amount().multiply(BigDecimal.valueOf(item.number())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
