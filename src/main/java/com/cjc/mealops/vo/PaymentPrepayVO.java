package com.cjc.mealops.vo;

import java.math.BigDecimal;

public record PaymentPrepayVO(Long paymentId, Long orderId, String paymentNo, BigDecimal amount, Integer status) {
}
