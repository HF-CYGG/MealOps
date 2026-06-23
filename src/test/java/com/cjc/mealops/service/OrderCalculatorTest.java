package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.cjc.mealops.dto.CartItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderCalculatorTest {

    @Test
    void sumsCartItemsByPriceAndQuantity() {
        OrderCalculator calculator = new OrderCalculator();
        List<CartItem> items = List.of(
                new CartItem(1L, null, "米饭", new BigDecimal("2.00"), 2),
                new CartItem(2L, null, "红烧牛肉", new BigDecimal("28.50"), 1));

        assertThat(calculator.total(items)).isEqualByComparingTo("32.50");
    }
}
