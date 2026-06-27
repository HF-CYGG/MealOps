package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.Dish;
import com.cjc.mealops.mapper.CategoryMapper;
import com.cjc.mealops.mapper.DiningCartItemMapper;
import com.cjc.mealops.mapper.DishFlavorMapper;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.mapper.SetmealDishMapper;
import com.cjc.mealops.mapper.ShoppingCartMapper;
import com.cjc.mealops.service.impl.DishServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DishDeletionPolicyTest {
    @Mock
    private DishMapper dishMapper;
    @Mock
    private DishFlavorMapper dishFlavorMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private SetmealDishMapper setmealDishMapper;
    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @Mock
    private DiningCartItemMapper diningCartItemMapper;
    @Mock
    private OrderDetailMapper orderDetailMapper;

    @Test
    void rejectsDeletingEnabledDish() {
        List<Long> ids = List.of(1001L);
        when(dishMapper.selectCount(org.mockito.ArgumentMatchers.<Wrapper<Dish>>any())).thenReturn(1L);
        DishServiceImpl service = service();

        assertThatThrownBy(() -> service.deleteBatch(ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Enabled dishes cannot be deleted");

        verifyNoInteractions(setmealDishMapper, shoppingCartMapper, diningCartItemMapper, orderDetailMapper);
        verify(dishFlavorMapper, never()).deleteByDishIds(ids);
    }

    @Test
    void rejectsDeletingDishReferencedBySetmealDish() {
        List<Long> ids = List.of(1002L);
        givenNoEnabledDish();
        when(setmealDishMapper.countByDishIds(ids)).thenReturn(1L);
        DishServiceImpl service = service();

        assertThatThrownBy(() -> service.deleteBatch(ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("set meal");

        verify(dishFlavorMapper, never()).deleteByDishIds(ids);
    }

    @Test
    void rejectsDeletingDishReferencedByShoppingCart() {
        List<Long> ids = List.of(1003L);
        givenNoEnabledDish();
        when(setmealDishMapper.countByDishIds(ids)).thenReturn(0L);
        when(shoppingCartMapper.countByDishIds(ids)).thenReturn(1L);
        DishServiceImpl service = service();

        assertThatThrownBy(() -> service.deleteBatch(ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("shopping cart");

        verify(dishFlavorMapper, never()).deleteByDishIds(ids);
    }

    @Test
    void rejectsDeletingDishReferencedByDiningCartItem() {
        List<Long> ids = List.of(1004L);
        givenNoEnabledDish();
        when(setmealDishMapper.countByDishIds(ids)).thenReturn(0L);
        when(shoppingCartMapper.countByDishIds(ids)).thenReturn(0L);
        when(diningCartItemMapper.countByDishIds(ids)).thenReturn(1L);
        DishServiceImpl service = service();

        assertThatThrownBy(() -> service.deleteBatch(ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("dining cart");

        verify(dishFlavorMapper, never()).deleteByDishIds(ids);
    }

    @Test
    void rejectsDeletingDishReferencedByOrderDetail() {
        List<Long> ids = List.of(1005L);
        givenNoEnabledDish();
        when(setmealDishMapper.countByDishIds(ids)).thenReturn(0L);
        when(shoppingCartMapper.countByDishIds(ids)).thenReturn(0L);
        when(diningCartItemMapper.countByDishIds(ids)).thenReturn(0L);
        when(orderDetailMapper.countByDishIds(ids)).thenReturn(1L);
        DishServiceImpl service = service();

        assertThatThrownBy(() -> service.deleteBatch(ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("order detail");

        verify(dishFlavorMapper, never()).deleteByDishIds(ids);
    }

    @Test
    void deletesStoppedUnreferencedDishBeforeDeletingFlavors() {
        List<Long> ids = List.of(1006L, 1007L);
        givenNoEnabledDish();
        when(setmealDishMapper.countByDishIds(ids)).thenReturn(0L);
        when(shoppingCartMapper.countByDishIds(ids)).thenReturn(0L);
        when(diningCartItemMapper.countByDishIds(ids)).thenReturn(0L);
        when(orderDetailMapper.countByDishIds(ids)).thenReturn(0L);
        DishServiceImpl service = org.mockito.Mockito.spy(service());
        doReturn(true).when(service).removeByIds(ids);

        service.deleteBatch(ids);

        InOrder inOrder = inOrder(service, dishFlavorMapper);
        inOrder.verify(service).removeByIds(ids);
        inOrder.verify(dishFlavorMapper).deleteByDishIds(ids);
    }

    private void givenNoEnabledDish() {
        when(dishMapper.selectCount(org.mockito.ArgumentMatchers.<Wrapper<Dish>>any())).thenReturn(0L);
    }

    private DishServiceImpl service() {
        DishServiceImpl service = new DishServiceImpl(
                dishFlavorMapper,
                categoryMapper,
                setmealDishMapper,
                shoppingCartMapper,
                diningCartItemMapper,
                orderDetailMapper);
        ReflectionTestUtils.setField(service, "baseMapper", dishMapper);
        return service;
    }
}
