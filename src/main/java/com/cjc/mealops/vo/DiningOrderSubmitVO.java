package com.cjc.mealops.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DiningOrderSubmitVO(Long orderId, String orderNumber, BigDecimal amount, LocalDateTime orderTime) {
}
