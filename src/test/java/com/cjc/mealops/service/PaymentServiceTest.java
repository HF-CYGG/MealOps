package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.Orders;
import com.cjc.mealops.entity.PaymentOrder;
import com.cjc.mealops.mapper.OrdersMapper;
import com.cjc.mealops.mapper.PaymentOrderMapper;
import com.cjc.mealops.service.impl.PaymentServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentOrderMapper paymentOrderMapper;
    @Mock
    private OrdersMapper ordersMapper;

    @AfterEach
    void clearContext() {
        BaseContext.clear();
    }

    @Test
    void confirmPaidPaymentIsIdempotentAndDoesNotUpdateOrderAgain() {
        BaseContext.setCurrentId(101L);
        BaseContext.setCurrentRole("USER");
        PaymentOrder paid = new PaymentOrder();
        paid.setId(9L);
        paid.setOrderId(100L);
        paid.setAmount(new BigDecimal("56.00"));
        paid.setStatus(PaymentOrder.PAID);
        when(paymentOrderMapper.selectById(9L)).thenReturn(paid);
        Orders order = new Orders();
        order.setId(100L);
        order.setUserId(101L);
        when(ordersMapper.selectById(100L)).thenReturn(order);

        PaymentServiceImpl service = new PaymentServiceImpl(paymentOrderMapper, ordersMapper);

        PaymentOrder result = service.confirm(9L);

        assertThat(result.getStatus()).isEqualTo(PaymentOrder.PAID);
        verify(paymentOrderMapper, never()).updateById(any(PaymentOrder.class));
        verify(ordersMapper, never()).updateById(any(Orders.class));
    }

    @Test
    void confirmPendingPaymentMarksPaymentAndOrderPaid() {
        BaseContext.setCurrentId(101L);
        BaseContext.setCurrentRole("USER");
        PaymentOrder pending = new PaymentOrder();
        pending.setId(9L);
        pending.setOrderId(100L);
        pending.setAmount(new BigDecimal("56.00"));
        pending.setStatus(PaymentOrder.PENDING);
        when(paymentOrderMapper.selectById(9L)).thenReturn(pending);

        Orders order = new Orders();
        order.setId(100L);
        order.setUserId(101L);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        when(ordersMapper.selectById(100L)).thenReturn(order);
        when(paymentOrderMapper.markPendingPaid(9L, PaymentOrder.PENDING, PaymentOrder.PAID)).thenReturn(1);

        PaymentServiceImpl service = new PaymentServiceImpl(paymentOrderMapper, ordersMapper);

        PaymentOrder result = service.confirm(9L);

        assertThat(result.getStatus()).isEqualTo(PaymentOrder.PAID);
        assertThat(result.getPaidAt()).isNotNull();
        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getPayStatus()).isEqualTo(Orders.PAID);
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(Orders.TO_BE_CONFIRMED);
    }

    @Test
    void confirmExpiredPendingPaymentCancelsOrderAndPayment() {
        BaseContext.setCurrentId(101L);
        BaseContext.setCurrentRole("USER");
        PaymentOrder pending = new PaymentOrder();
        pending.setId(9L);
        pending.setOrderId(100L);
        pending.setAmount(new BigDecimal("56.00"));
        pending.setStatus(PaymentOrder.PENDING);
        when(paymentOrderMapper.selectById(9L)).thenReturn(pending);

        Orders order = new Orders();
        order.setId(100L);
        order.setUserId(101L);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now().minusMinutes(16));
        when(ordersMapper.selectById(100L)).thenReturn(order);

        PaymentServiceImpl service = new PaymentServiceImpl(paymentOrderMapper, ordersMapper);

        assertThatThrownBy(() -> service.confirm(9L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Payment expired");

        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(Orders.CANCELLED);
        assertThat(orderCaptor.getValue().getCancelReason()).isEqualTo("Payment expired automatically");
        verify(paymentOrderMapper).updateById(any(PaymentOrder.class));
        verify(paymentOrderMapper, never()).markPendingPaid(9L, PaymentOrder.PENDING, PaymentOrder.PAID);
    }

    @Test
    void confirmCancelledPaymentIsRejected() {
        BaseContext.setCurrentId(101L);
        BaseContext.setCurrentRole("USER");
        PaymentOrder cancelled = new PaymentOrder();
        cancelled.setId(9L);
        cancelled.setOrderId(100L);
        cancelled.setStatus(PaymentOrder.CANCELLED);
        when(paymentOrderMapper.selectById(9L)).thenReturn(cancelled);

        Orders order = new Orders();
        order.setId(100L);
        order.setUserId(101L);
        when(ordersMapper.selectById(100L)).thenReturn(order);

        PaymentServiceImpl service = new PaymentServiceImpl(paymentOrderMapper, ordersMapper);

        assertThatThrownBy(() -> service.confirm(9L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Payment is not pending");
        verify(paymentOrderMapper, never()).markPendingPaid(9L, PaymentOrder.PENDING, PaymentOrder.PAID);
        verify(ordersMapper, never()).updateById(any(Orders.class));
    }

    @Test
    void userCannotConfirmAnotherUsersPayment() {
        BaseContext.setCurrentId(202L);
        BaseContext.setCurrentRole("USER");
        PaymentOrder pending = new PaymentOrder();
        pending.setId(9L);
        pending.setOrderId(100L);
        pending.setStatus(PaymentOrder.PENDING);
        when(paymentOrderMapper.selectById(9L)).thenReturn(pending);

        Orders order = new Orders();
        order.setId(100L);
        order.setUserId(101L);
        when(ordersMapper.selectById(100L)).thenReturn(order);

        PaymentServiceImpl service = new PaymentServiceImpl(paymentOrderMapper, ordersMapper);

        assertThatThrownBy(() -> service.confirm(9L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Permission denied");
        verify(paymentOrderMapper, never()).markPendingPaid(9L, PaymentOrder.PENDING, PaymentOrder.PAID);
        verify(ordersMapper, never()).updateById(any(Orders.class));
    }
}
