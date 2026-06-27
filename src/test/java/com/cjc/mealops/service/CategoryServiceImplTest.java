package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.Category;
import com.cjc.mealops.mapper.CategoryMapper;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.SetmealMapper;
import com.cjc.mealops.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CategoryServiceImplTest {
    @Test
    void rejectsStatusUpdateWhenCategoryDoesNotExist() {
        CategoryMapper categoryMapper = mock(CategoryMapper.class);
        when(categoryMapper.updateById(org.mockito.ArgumentMatchers.any(Category.class))).thenReturn(0);
        CategoryServiceImpl service = new CategoryServiceImpl(
                mock(DishMapper.class),
                mock(SetmealMapper.class),
                new CategoryDeletionPolicy()
        );
        ReflectionTestUtils.setField(service, "baseMapper", categoryMapper);

        assertThatThrownBy(() -> service.updateStatus(0, 1001L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Category not found");
    }
}
