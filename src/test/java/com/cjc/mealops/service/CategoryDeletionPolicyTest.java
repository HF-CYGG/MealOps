package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cjc.mealops.common.BusinessException;
import org.junit.jupiter.api.Test;

class CategoryDeletionPolicyTest {

    @Test
    void rejectsDeletingCategoryWhenDishOrSetmealExists() {
        CategoryDeletionPolicy policy = new CategoryDeletionPolicy();

        assertThatThrownBy(() -> policy.check(1, 1, 0))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("菜品");

        assertThatThrownBy(() -> policy.check(2, 0, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("套餐");
    }
}
