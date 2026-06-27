package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.OrderDetail;
import com.cjc.mealops.entity.Orders;
import com.cjc.mealops.mapper.AddressBookMapper;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.mapper.OrdersMapper;
import com.cjc.mealops.mapper.SetmealDishMapper;
import com.cjc.mealops.mapper.UserMapper;
import com.cjc.mealops.service.impl.OrderServiceImpl;
import com.cjc.mealops.vo.OrderVO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OrderServiceImplTest {
    private OrdersMapper ordersMapper;
    private OrderDetailMapper orderDetailMapper;

    @AfterEach
    void clearContext() {
        BaseContext.clear();
    }

    @Test
    void detailReturnsOrderWithOrderDetailList() {
        OrderServiceImpl service = serviceWithOrder(ownedOrder());
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(9001L);
        detail.setName("宫保鸡丁");
        detail.setNumber(1);
        detail.setAmount(BigDecimal.valueOf(28));
        when(orderDetailMapper.selectList(any())).thenReturn(List.of(detail));
        BaseContext.setCurrentId(2070166539388665857L);
        BaseContext.setCurrentRole("USER");

        OrderVO result = service.detail(9001L);

        assertThat(result.getId()).isEqualTo(9001L);
        assertThat(result.getNumber()).isEqualTo("202606270001");
        assertThat(result.getConsignee()).isEqualTo("Yan");
        assertThat(result.getAmount()).isEqualByComparingTo("28");
        assertThat(result.getOrderDetailList()).containsExactly(detail);
    }

    @Test
    void detailRejectsDifferentUserOrder() {
        OrderServiceImpl service = serviceWithOrder(ownedOrder());
        BaseContext.setCurrentId(1L);
        BaseContext.setCurrentRole("USER");

        assertThatThrownBy(() -> service.detail(9001L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Order not found");
        verify(orderDetailMapper, never()).selectList(any());
    }

    @Test
    void detailRejectsMissingCurrentUser() {
        OrderServiceImpl service = serviceWithOrder(ownedOrder());
        BaseContext.setCurrentRole("USER");

        assertThatThrownBy(() -> service.detail(9001L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Order not found");
        verify(orderDetailMapper, never()).selectList(any());
    }

    @Test
    void detailRejectsUnknownRole() {
        OrderServiceImpl service = serviceWithOrder(ownedOrder());
        BaseContext.setCurrentId(2070166539388665857L);
        BaseContext.setCurrentRole("GUEST");

        assertThatThrownBy(() -> service.detail(9001L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Permission denied");
        verify(orderDetailMapper, never()).selectList(any());
    }

    @Test
    void detailAllowsEmployeeToViewAnyOrder() {
        OrderServiceImpl service = serviceWithOrder(ownedOrder());
        BaseContext.setCurrentId(1L);
        BaseContext.setCurrentRole("EMPLOYEE");
        when(orderDetailMapper.selectList(any())).thenReturn(List.of());

        OrderVO result = service.detail(9001L);

        assertThat(result.getId()).isEqualTo(9001L);
        verify(orderDetailMapper).selectList(any());
    }

    private Orders ownedOrder() {
        Orders order = new Orders();
        order.setId(9001L);
        order.setNumber("202606270001");
        order.setUserId(2070166539388665857L);
        order.setConsignee("Yan");
        order.setAmount(BigDecimal.valueOf(28));
        return order;
    }

    private OrderServiceImpl serviceWithOrder(Orders order) {
        ordersMapper = mock(OrdersMapper.class);
        orderDetailMapper = mock(OrderDetailMapper.class);
        when(ordersMapper.selectById(9001L)).thenReturn(order);
        OrderServiceImpl service = new OrderServiceImpl(
                mock(ShoppingCartService.class),
                mock(AddressBookMapper.class),
                mock(UserMapper.class),
                orderDetailMapper,
                mock(DishMapper.class),
                mock(SetmealDishMapper.class),
                mock(OrderCalculator.class));
        ReflectionTestUtils.setField(service, "baseMapper", ordersMapper);
        return service;
    }
}
