package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjc.mealops.entity.Category;
import com.cjc.mealops.entity.Dish;
import com.cjc.mealops.mapper.CategoryMapper;
import com.cjc.mealops.mapper.DiningCartItemMapper;
import com.cjc.mealops.mapper.DishFlavorMapper;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.mapper.SetmealDishMapper;
import com.cjc.mealops.mapper.ShoppingCartMapper;
import com.cjc.mealops.service.impl.DishServiceImpl;
import com.cjc.mealops.vo.DishVO;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DishServiceImplPageQueryTest {
    @Test
    void pageQueryFiltersDishesAndReturnsCategoryName() {
        DishMapper dishMapper = mock(DishMapper.class);
        CategoryMapper categoryMapper = mock(CategoryMapper.class);
        Dish dish = new Dish();
        dish.setId(3001L);
        dish.setName("宫保鸡丁");
        dish.setCategoryId(1001L);
        dish.setPrice(BigDecimal.valueOf(28));
        dish.setStatus(1);
        when(dishMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<Dish> page = invocation.getArgument(0);
            page.setRecords(List.of(dish));
            page.setTotal(1);
            return page;
        });
        Category category = new Category();
        category.setId(1001L);
        category.setName("热菜");
        when(categoryMapper.selectBatchIds(any(Collection.class))).thenReturn(List.of(category));
        DishServiceImpl service = new DishServiceImpl(
                mock(DishFlavorMapper.class),
                categoryMapper,
                mock(SetmealDishMapper.class),
                mock(ShoppingCartMapper.class),
                mock(DiningCartItemMapper.class),
                mock(OrderDetailMapper.class));
        ReflectionTestUtils.setField(service, "baseMapper", dishMapper);

        Page<DishVO> result = service.pageQuery(Map.of(
                "page", "2",
                "pageSize", "5",
                "name", "宫保",
                "categoryId", "1001",
                "status", "1"));

        assertThat(result.getCurrent()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).singleElement()
                .satisfies(item -> {
                    assertThat(item.getId()).isEqualTo(3001L);
                    assertThat(item.getCategoryName()).isEqualTo("热菜");
        });
        org.mockito.Mockito.verify(dishMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }
}
