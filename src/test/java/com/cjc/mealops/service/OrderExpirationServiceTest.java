package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cjc.mealops.entity.OrderDetail;
import com.cjc.mealops.entity.Orders;
import com.cjc.mealops.entity.PaymentOrder;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.mapper.OrdersMapper;
import com.cjc.mealops.mapper.PaymentOrderMapper;
import com.cjc.mealops.service.impl.OrderExpirationService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OrderExpirationServiceTest {
    @Test
    void expireOverdueOrdersCancelsOrdersPaymentsAndRestoresDishStock() {
        OrdersMapper ordersMapper = mock(OrdersMapper.class);
        PaymentOrderMapper paymentOrderMapper = mock(PaymentOrderMapper.class);
        OrderDetailMapper orderDetailMapper = mock(OrderDetailMapper.class);
        DishMapper dishMapper = mock(DishMapper.class);

        Orders order = new Orders();
        order.setId(100L);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now().minusMinutes(16));
        when(ordersMapper.selectList(any())).thenReturn(List.of(order));

        PaymentOrder payment = new PaymentOrder();
        payment.setId(200L);
        payment.setOrderId(100L);
        payment.setStatus(PaymentOrder.PENDING);
        when(paymentOrderMapper.selectLatestByOrderId(100L)).thenReturn(payment);

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(100L);
        detail.setDishId(300L);
        detail.setNumber(2);
        when(orderDetailMapper.selectList(any())).thenReturn(List.of(detail));

        OrderExpirationService service = new OrderExpirationService(
                ordersMapper, paymentOrderMapper, orderDetailMapper, dishMapper);

        int expired = service.expireOverdueOrders();

        assertThat(expired).isEqualTo(1);
        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(Orders.CANCELLED);
        verify(paymentOrderMapper).updateById(any(PaymentOrder.class));
        verify(dishMapper).restoreStock(300L, 2);
    }
}
