package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.DiningCartItem;
import com.cjc.mealops.entity.DiningSession;
import com.cjc.mealops.entity.Dish;
import com.cjc.mealops.entity.OrderDetail;
import com.cjc.mealops.entity.Orders;
import com.cjc.mealops.mapper.DiningCartItemMapper;
import com.cjc.mealops.mapper.DiningSessionMapper;
import com.cjc.mealops.mapper.DiningSessionMemberMapper;
import com.cjc.mealops.mapper.DiningTableMapper;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.mapper.OrdersMapper;
import com.cjc.mealops.mapper.SetmealDishMapper;
import com.cjc.mealops.mapper.SetmealMapper;
import com.cjc.mealops.service.impl.DiningSessionServiceImpl;
import com.cjc.mealops.vo.DiningOrderSubmitVO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

@ExtendWith(MockitoExtension.class)
class DiningSessionServiceTest {
    @Mock
    private DiningSessionMapper diningSessionMapper;
    @Mock
    private DiningSessionMemberMapper diningSessionMemberMapper;
    @Mock
    private DiningTableMapper diningTableMapper;
    @Mock
    private DiningCartItemMapper diningCartItemMapper;
    @Mock
    private OrdersMapper ordersMapper;
    @Mock
    private OrderDetailMapper orderDetailMapper;
    @Mock
    private DishMapper dishMapper;
    @Mock
    private SetmealMapper setmealMapper;
    @Mock
    private SetmealDishMapper setmealDishMapper;

    @AfterEach
    void clearContext() {
        BaseContext.clear();
    }

    @Test
    void submitOrderCalculatesAmountFromServerSnapshotsDeductsStockAndClearsCart() {
        BaseContext.setCurrentId(101L);
        DiningSession session = new DiningSession();
        session.setId(10L);
        session.setTableId(2L);
        session.setTableName("A02");
        session.setPartySize(3);
        session.setStatus(DiningSession.ACTIVE);
        when(diningSessionMapper.selectById(10L)).thenReturn(session);
        when(diningSessionMapper.selectForUpdate(10L)).thenReturn(session);
        when(diningSessionMemberMapper.countActiveMember(10L, 101L)).thenReturn(1L);

        Dish dish = new Dish();
        dish.setId(3001L);
        dish.setName("宫保鸡丁");
        dish.setImage("/images/dish-gongbao.jpg");
        dish.setPrice(new BigDecimal("28.00"));
        dish.setStatus(1);
        when(dishMapper.selectById(3001L)).thenReturn(dish);
        when(dishMapper.deductStock(3001L, 2)).thenReturn(1);

        DiningCartItem cartItem = new DiningCartItem();
        cartItem.setId(88L);
        cartItem.setSessionId(10L);
        cartItem.setCreatorUserId(101L);
        cartItem.setDishId(3001L);
        cartItem.setName("stale client name");
        cartItem.setAmount(new BigDecimal("0.01"));
        cartItem.setNumber(2);
        when(diningCartItemMapper.selectActiveItems(10L)).thenReturn(List.of(cartItem));

        DiningSessionServiceImpl service = new DiningSessionServiceImpl(
                diningSessionMapper,
                diningSessionMemberMapper,
                diningTableMapper,
                diningCartItemMapper,
                ordersMapper,
                orderDetailMapper,
                dishMapper,
                setmealMapper,
                setmealDishMapper);

        DiningOrderSubmitVO result = service.submitOrder(10L);

        assertThat(result.amount()).isEqualByComparingTo("56.00");
        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersMapper).insert(orderCaptor.capture());
        Orders order = orderCaptor.getValue();
        assertThat(order.getOrderType()).isEqualTo(Orders.TYPE_DINE_IN);
        assertThat(order.getDiningSessionId()).isEqualTo(10L);
        assertThat(order.getTableId()).isEqualTo(2L);
        assertThat(order.getTableName()).isEqualTo("A02");
        assertThat(order.getPartySize()).isEqualTo(3);
        assertThat(order.getAmount()).isEqualByComparingTo("56.00");

        ArgumentCaptor<OrderDetail> detailCaptor = ArgumentCaptor.forClass(OrderDetail.class);
        verify(orderDetailMapper).insert(detailCaptor.capture());
        OrderDetail detail = detailCaptor.getValue();
        assertThat(detail.getName()).isEqualTo("宫保鸡丁");
        assertThat(detail.getAmount()).isEqualByComparingTo("28.00");
        assertThat(detail.getCreatorUserId()).isEqualTo(101L);
        assertThat(detail.getDiningCartItemId()).isEqualTo(88L);
        verify(dishMapper).deductStock(3001L, 2);
        verify(diningCartItemMapper).deleteBySessionId(10L);
    }

    @Test
    void updateCartItemRejectsItemsCreatedByAnotherUser() {
        BaseContext.setCurrentId(202L);
        DiningSession session = new DiningSession();
        session.setId(10L);
        session.setStatus(DiningSession.ACTIVE);
        when(diningSessionMapper.selectById(10L)).thenReturn(session);
        when(diningSessionMemberMapper.countActiveMember(10L, 202L)).thenReturn(1L);

        DiningCartItem cartItem = new DiningCartItem();
        cartItem.setId(88L);
        cartItem.setSessionId(10L);
        cartItem.setCreatorUserId(101L);
        cartItem.setDishId(3001L);
        cartItem.setNumber(1);
        when(diningCartItemMapper.selectById(88L)).thenReturn(cartItem);

        DiningSessionServiceImpl service = new DiningSessionServiceImpl(
                diningSessionMapper,
                diningSessionMemberMapper,
                diningTableMapper,
                diningCartItemMapper,
                ordersMapper,
                orderDetailMapper,
                dishMapper,
                setmealMapper,
                setmealDishMapper);

        assertThatThrownBy(() -> service.updateCartItem(10L, 88L, 3))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only creator can change dining cart item");
        verify(diningCartItemMapper, never()).updateById(any(DiningCartItem.class));
    }
}
