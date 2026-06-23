package com.cjc.mealops.service;

import com.cjc.mealops.common.BusinessException;

public class CategoryDeletionPolicy {

    public void check(Integer categoryType, long dishCount, long setmealCount) {
        if (dishCount > 0) {
            throw new BusinessException("当前分类下关联了菜品，不能删除");
        }
        if (setmealCount > 0) {
            throw new BusinessException("当前分类下关联了套餐，不能删除");
        }
    }
}
