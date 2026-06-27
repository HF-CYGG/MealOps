package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cjc.mealops.entity.OrderDetail;
import com.cjc.mealops.entity.Orders;
import com.cjc.mealops.entity.PaymentOrder;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.mapper.OrdersMapper;
import com.cjc.mealops.mapper.PaymentOrderMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderExpirationService {
    public static final int PAYMENT_RETENTION_MINUTES = 15;
    public static final String EXPIRED_CANCEL_REASON = "Payment expired automatically";

    private final OrdersMapper ordersMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final DishMapper dishMapper;

    public OrderExpirationService(OrdersMapper ordersMapper,
                                  PaymentOrderMapper paymentOrderMapper,
                                  OrderDetailMapper orderDetailMapper,
                                  DishMapper dishMapper) {
        this.ordersMapper = ordersMapper;
        this.paymentOrderMapper = paymentOrderMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.dishMapper = dishMapper;
    }

    @Scheduled(fixedDelay = 60_000L, initialDelay = 60_000L)
    @Transactional(rollbackFor = Exception.class)
    public void expireOverdueOrdersOnSchedule() {
        expireOverdueOrders();
    }

    @Transactional(rollbackFor = Exception.class)
    public int expireOverdueOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(PAYMENT_RETENTION_MINUTES);
        List<Orders> orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, Orders.PENDING_PAYMENT)
                .eq(Orders::getPayStatus, Orders.UN_PAID)
                .le(Orders::getOrderTime, deadline));
        int expired = 0;
        for (Orders order : orders) {
            PaymentOrder payment = paymentOrderMapper.selectLatestByOrderId(order.getId());
            if (expireOrder(order, payment)) {
                expired++;
            }
        }
        return expired;
    }

    public boolean isExpired(Orders order) {
        return isExpired(order, LocalDateTime.now());
    }

    public boolean isExpired(Orders order, LocalDateTime now) {
        if (order == null
                || order.getOrderTime() == null
                || !Integer.valueOf(Orders.PENDING_PAYMENT).equals(order.getStatus())
                || !Integer.valueOf(Orders.UN_PAID).equals(order.getPayStatus())) {
            return false;
        }
        return !order.getOrderTime().plusMinutes(PAYMENT_RETENTION_MINUTES).isAfter(now);
    }

    public boolean expireOrder(Orders order, PaymentOrder payment) {
        if (order == null
                || !Integer.valueOf(Orders.PENDING_PAYMENT).equals(order.getStatus())
                || !Integer.valueOf(Orders.UN_PAID).equals(order.getPayStatus())) {
            return false;
        }
        if (Orders.TYPE_DINE_IN.equals(order.getOrderType())) {
            restoreDishStock(order.getId());
        }
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(EXPIRED_CANCEL_REASON);
        order.setCancelTime(now);
        ordersMapper.updateById(order);
        if (payment != null && Integer.valueOf(PaymentOrder.PENDING).equals(payment.getStatus())) {
            payment.setStatus(PaymentOrder.CANCELLED);
            payment.setUpdateTime(now);
            paymentOrderMapper.updateById(payment);
        }
        return true;
    }

    private void restoreDishStock(Long orderId) {
        if (orderDetailMapper == null || dishMapper == null) {
            return;
        }
        List<OrderDetail> details = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, orderId));
        for (OrderDetail detail : details) {
            if (detail.getDishId() != null && detail.getNumber() != null && detail.getNumber() > 0) {
                dishMapper.restoreStock(detail.getDishId(), detail.getNumber());
            }
        }
    }
}
