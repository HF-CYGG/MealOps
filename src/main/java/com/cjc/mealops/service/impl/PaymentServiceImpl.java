package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.common.AuthUtils;
import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.OrderDetail;
import com.cjc.mealops.entity.Orders;
import com.cjc.mealops.entity.PaymentOrder;
import com.cjc.mealops.entity.SetmealDish;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.mapper.OrdersMapper;
import com.cjc.mealops.mapper.PaymentOrderMapper;
import com.cjc.mealops.mapper.SetmealDishMapper;
import com.cjc.mealops.service.PaymentService;
import com.cjc.mealops.vo.PaymentPrepayVO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentOrderMapper, PaymentOrder> implements PaymentService {
    private final PaymentOrderMapper paymentOrderMapper;
    private final OrdersMapper ordersMapper;
    private final OrderExpirationService orderExpirationService;
    private final OrderDetailMapper orderDetailMapper;
    private final DishMapper dishMapper;
    private final SetmealDishMapper setmealDishMapper;

    @Autowired
    public PaymentServiceImpl(PaymentOrderMapper paymentOrderMapper,
                              OrdersMapper ordersMapper,
                              OrderExpirationService orderExpirationService,
                              OrderDetailMapper orderDetailMapper,
                              DishMapper dishMapper,
                              SetmealDishMapper setmealDishMapper) {
        this.paymentOrderMapper = paymentOrderMapper;
        this.ordersMapper = ordersMapper;
        this.orderExpirationService = orderExpirationService;
        this.orderDetailMapper = orderDetailMapper;
        this.dishMapper = dishMapper;
        this.setmealDishMapper = setmealDishMapper;
    }

    public PaymentServiceImpl(PaymentOrderMapper paymentOrderMapper, OrdersMapper ordersMapper) {
        this.paymentOrderMapper = paymentOrderMapper;
        this.ordersMapper = ordersMapper;
        this.orderExpirationService = new OrderExpirationService(ordersMapper, paymentOrderMapper, null, null);
        this.orderDetailMapper = null;
        this.dishMapper = null;
        this.setmealDishMapper = null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPrepayVO prepay(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        requireOrderOwner(order);
        PaymentOrder existing = paymentOrderMapper.selectLatestByOrderId(orderId);
        if (orderExpirationService.isExpired(order)) {
            orderExpirationService.expireOrder(order, existing);
            throw new BusinessException("Payment expired");
        }
        if (existing != null) {
            return toPrepayVO(existing);
        }

        LocalDateTime now = LocalDateTime.now();
        PaymentOrder payment = new PaymentOrder();
        payment.setId(IdWorker.getId());
        payment.setOrderId(orderId);
        payment.setPaymentNo("PAY" + now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + orderId);
        payment.setAmount(order.getAmount());
        payment.setPayMethod(order.getPayMethod());
        payment.setStatus(PaymentOrder.PENDING);
        payment.setCreateTime(now);
        payment.setUpdateTime(now);
        paymentOrderMapper.insert(payment);
        return toPrepayVO(payment);
    }

    @Override
    public PaymentOrder getByOrderId(Long orderId) {
        PaymentOrder payment = paymentOrderMapper.selectLatestByOrderId(orderId);
        if (payment == null) {
            throw new BusinessException("Payment not found");
        }
        Orders order = ordersMapper.selectById(payment.getOrderId());
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        requireOrderOwner(order);
        if (orderExpirationService.isExpired(order)) {
            orderExpirationService.expireOrder(order, payment);
            throw new BusinessException("Payment expired");
        }
        return payment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentOrder confirm(Long paymentId) {
        PaymentOrder payment = paymentOrderMapper.selectById(paymentId);
        if (payment == null) {
            throw new BusinessException("Payment not found");
        }
        Orders order = ordersMapper.selectById(payment.getOrderId());
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        requireOrderOwner(order);
        if (PaymentOrder.PAID == payment.getStatus()) {
            return payment;
        }
        if (orderExpirationService.isExpired(order)) {
            orderExpirationService.expireOrder(order, payment);
            throw new BusinessException("Payment expired");
        }
        if (!Integer.valueOf(PaymentOrder.PENDING).equals(payment.getStatus())) {
            throw new BusinessException("Payment is not pending");
        }

        if (paymentOrderMapper.markPendingPaid(paymentId, PaymentOrder.PENDING, PaymentOrder.PAID) == 0) {
            PaymentOrder latest = paymentOrderMapper.selectById(paymentId);
            if (latest != null && PaymentOrder.PAID == latest.getStatus()) {
                return latest;
            }
            throw new BusinessException("Payment is not pending");
        }

        LocalDateTime now = LocalDateTime.now();
        deductStock(order.getId());
        order.setPayStatus(Orders.PAID);
        order.setCheckoutTime(now);
        if (Orders.PENDING_PAYMENT == order.getStatus()) {
            order.setStatus(Orders.TO_BE_CONFIRMED);
        }
        ordersMapper.updateById(order);
        payment.setStatus(PaymentOrder.PAID);
        payment.setPaidAt(now);
        payment.setUpdateTime(now);
        return payment;
    }

    @Override
    public Object page(Map<String, Object> params) {
        Page<PaymentOrder> page = new Page<>(longParam(params, "page", 1L), longParam(params, "pageSize", 10L));
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        Object status = params == null ? null : params.get("status");
        if (status != null && !String.valueOf(status).isBlank()) {
            wrapper.eq(PaymentOrder::getStatus, Integer.parseInt(String.valueOf(status)));
        }
        wrapper.orderByDesc(PaymentOrder::getCreateTime);
        return paymentOrderMapper.selectPage(page, wrapper);
    }

    private PaymentPrepayVO toPrepayVO(PaymentOrder payment) {
        return new PaymentPrepayVO(payment.getId(), payment.getOrderId(), payment.getPaymentNo(),
                payment.getAmount(), payment.getStatus());
    }

    private long longParam(Map<String, Object> params, String name, long defaultValue) {
        if (params == null || params.get(name) == null || String.valueOf(params.get(name)).isBlank()) {
            return defaultValue;
        }
        return Long.parseLong(String.valueOf(params.get(name)));
    }

    private void deductStock(Long orderId) {
        if (orderDetailMapper == null || dishMapper == null || setmealDishMapper == null) {
            return;
        }
        List<OrderDetail> details = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, orderId));
        if (CollectionUtils.isEmpty(details)) {
            throw new BusinessException("Order details are empty");
        }
        for (OrderDetail detail : details) {
            int number = positiveNumber(detail.getNumber());
            if (number == 0) {
                continue;
            }
            if (detail.getDishId() != null) {
                deductDishStock(detail.getDishId(), number);
                continue;
            }
            if (detail.getSetmealId() != null) {
                deductSetmealStock(detail.getSetmealId(), number);
            }
        }
    }

    private void deductSetmealStock(Long setmealId, int number) {
        List<SetmealDish> dishes = setmealDishMapper.selectBySetmealId(setmealId);
        if (CollectionUtils.isEmpty(dishes)) {
            throw new BusinessException("Setmeal dishes are empty");
        }
        for (SetmealDish item : dishes) {
            int copies = positiveNumber(item.getCopies());
            if (copies > 0) {
                deductDishStock(item.getDishId(), copies * number);
            }
        }
    }

    private void deductDishStock(Long dishId, Integer number) {
        if (dishMapper.deductStock(dishId, number) == 0) {
            throw new BusinessException("Dish stock is insufficient");
        }
    }

    private int positiveNumber(Integer number) {
        return number == null || number <= 0 ? 0 : number;
    }

    private void requireOrderOwner(Orders order) {
        if (AuthUtils.isEmployee()) {
            return;
        }
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null || !currentId.equals(order.getUserId())) {
            throw new BusinessException("Permission denied");
        }
    }
}
