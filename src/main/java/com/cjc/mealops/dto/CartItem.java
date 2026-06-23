package com.cjc.mealops.dto;

import java.math.BigDecimal;

public record CartItem(Long dishId, Long setmealId, String name, BigDecimal amount, Integer number) {
}
