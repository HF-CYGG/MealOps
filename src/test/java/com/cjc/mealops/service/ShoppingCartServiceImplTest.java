package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.dto.CartItem;
import com.cjc.mealops.entity.ShoppingCart;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.SetmealMapper;
import com.cjc.mealops.mapper.ShoppingCartMapper;
import com.cjc.mealops.service.impl.ShoppingCartServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class ShoppingCartServiceImplTest {

    @AfterEach
    void clearContext() {
        BaseContext.clear();
    }

    @Test
    void addCartItemDefaultsMissingNumberToOne() {
        ShoppingCartMapper cartMapper = mock(ShoppingCartMapper.class);
        when(cartMapper.selectList(any())).thenReturn(List.of());
        when(cartMapper.insert(any(ShoppingCart.class))).thenReturn(1);
        ShoppingCartServiceImpl service = new ShoppingCartServiceImpl(mock(DishMapper.class), mock(SetmealMapper.class));
        ReflectionTestUtils.setField(service, "baseMapper", cartMapper);
        BaseContext.setCurrentId(2070166539388665857L);

        service.add(new CartItem(1003L, null, "番茄炒蛋", BigDecimal.valueOf(18), null));

        ArgumentCaptor<ShoppingCart> captor = ArgumentCaptor.forClass(ShoppingCart.class);
        verify(cartMapper).insert(captor.capture());
        assertThat(captor.getValue().getNumber()).isEqualTo(1);
    }
}
