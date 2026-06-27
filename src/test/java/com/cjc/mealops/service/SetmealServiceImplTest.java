package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.Category;
import com.cjc.mealops.entity.Setmeal;
import com.cjc.mealops.mapper.CategoryMapper;
import com.cjc.mealops.mapper.SetmealDishMapper;
import com.cjc.mealops.mapper.SetmealMapper;
import com.cjc.mealops.service.impl.SetmealServiceImpl;
import com.cjc.mealops.vo.SetmealVO;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SetmealServiceImplTest {
    @Test
    void rejectsStatusUpdateWhenSetmealDoesNotExist() {
        SetmealMapper setmealMapper = mock(SetmealMapper.class);
        when(setmealMapper.updateById(org.mockito.ArgumentMatchers.any(Setmeal.class))).thenReturn(0);
        SetmealServiceImpl service = new SetmealServiceImpl(
                mock(SetmealDishMapper.class),
                mock(CategoryMapper.class)
        );
        ReflectionTestUtils.setField(service, "baseMapper", setmealMapper);

        assertThatThrownBy(() -> service.updateStatus(0, List.of(5001L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Setmeal not found");
    }

    @Test
    void pageQueryFiltersSetmealsAndReturnsCategoryName() {
        SetmealMapper setmealMapper = mock(SetmealMapper.class);
        CategoryMapper categoryMapper = mock(CategoryMapper.class);
        Setmeal setmeal = new Setmeal();
        setmeal.setId(5001L);
        setmeal.setName("Lunch Combo");
        setmeal.setCategoryId(2001L);
        setmeal.setPrice(BigDecimal.valueOf(38));
        setmeal.setStatus(1);
        when(setmealMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<Setmeal> page = invocation.getArgument(0);
            page.setRecords(List.of(setmeal));
            page.setTotal(1);
            return page;
        });
        Category category = new Category();
        category.setId(2001L);
        category.setName("Combos");
        when(categoryMapper.selectBatchIds(any(Collection.class))).thenReturn(List.of(category));
        SetmealServiceImpl service = new SetmealServiceImpl(mock(SetmealDishMapper.class), categoryMapper);
        ReflectionTestUtils.setField(service, "baseMapper", setmealMapper);

        Page<SetmealVO> result = service.pageQuery(Map.of(
                "page", "1",
                "pageSize", "10",
                "name", "Lunch",
                "categoryId", "2001",
                "status", "1"));

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).singleElement()
                .satisfies(item -> {
                    assertThat(item.getId()).isEqualTo(5001L);
                    assertThat(item.getCategoryName()).isEqualTo("Combos");
                });
    }
}
