package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.common.AuthUtils;
import com.cjc.mealops.dto.CartItem;
import com.cjc.mealops.dto.OrdersSubmitDTO;
import com.cjc.mealops.entity.AddressBook;
import com.cjc.mealops.entity.OrderDetail;
import com.cjc.mealops.entity.Orders;
import com.cjc.mealops.entity.SetmealDish;
import com.cjc.mealops.entity.ShoppingCart;
import com.cjc.mealops.entity.User;
import com.cjc.mealops.mapper.AddressBookMapper;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.mapper.OrdersMapper;
import com.cjc.mealops.mapper.SetmealDishMapper;
import com.cjc.mealops.mapper.UserMapper;
import com.cjc.mealops.service.OrderCalculator;
import com.cjc.mealops.service.OrderService;
import com.cjc.mealops.service.ShoppingCartService;
import com.cjc.mealops.vo.OrderSubmitVO;
import com.cjc.mealops.vo.OrderVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ShoppingCartService shoppingCartService;
    private final AddressBookMapper addressBookMapper;
    private final UserMapper userMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final DishMapper dishMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final OrderCalculator orderCalculator;

    public OrderServiceImpl(ShoppingCartService shoppingCartService,
                            AddressBookMapper addressBookMapper,
                            UserMapper userMapper,
                            OrderDetailMapper orderDetailMapper,
                            DishMapper dishMapper,
                            SetmealDishMapper setmealDishMapper,
                            OrderCalculator orderCalculator) {
        this.shoppingCartService = shoppingCartService;
        this.addressBookMapper = addressBookMapper;
        this.userMapper = userMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.dishMapper = dishMapper;
        this.setmealDishMapper = setmealDishMapper;
        this.orderCalculator = orderCalculator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = requiredCurrentUserId();
        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null || !userId.equals(addressBook.getUserId())) {
            throw new BusinessException("Address not found");
        }

        List<ShoppingCart> carts = shoppingCartService.listCurrentUserCart();
        if (CollectionUtils.isEmpty(carts)) {
            throw new BusinessException("Shopping cart is empty");
        }
        deductStock(carts);

        List<CartItem> cartItems = carts.stream()
                .map(item -> new CartItem(item.getDishId(), item.getSetmealId(),
                        item.getName(), item.getAmount(), item.getNumber()))
                .toList();
        BigDecimal amount = orderCalculator.total(cartItems);
        LocalDateTime now = LocalDateTime.now();

        Orders orders = new Orders();
        orders.setNumber(generateOrderNumber(userId));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setOrderType(Orders.TYPE_TAKEOUT);
        orders.setUserId(userId);
        orders.setAddressBookId(addressBook.getId());
        orders.setOrderTime(now);
        orders.setPayMethod(ordersSubmitDTO.getPayMethod());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setAmount(amount);
        orders.setRemark(ordersSubmitDTO.getRemark());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(formatAddress(addressBook));
        orders.setConsignee(addressBook.getConsignee());
        orders.setEstimatedDeliveryTime(ordersSubmitDTO.getEstimatedDeliveryTime());
        orders.setDeliveryStatus(ordersSubmitDTO.getDeliveryStatus());
        orders.setPackAmount(ordersSubmitDTO.getPackAmount());
        orders.setTablewareNumber(ordersSubmitDTO.getTablewareNumber());
        orders.setTablewareStatus(ordersSubmitDTO.getTablewareStatus());
        User user = userMapper.selectById(userId);
        if (user != null) {
            orders.setUserName(user.getName());
        }
        save(orders);

        for (ShoppingCart cart : carts) {
            orderDetailMapper.insert(toOrderDetail(cart, orders.getId()));
        }
        shoppingCartService.clean();
        return new OrderSubmitVO(orders.getId(), orders.getNumber(), orders.getAmount(), orders.getOrderTime());
    }

    private void deductStock(List<ShoppingCart> carts) {
        for (ShoppingCart cart : carts) {
            if (cart.getDishId() != null) {
                deductDishStock(cart.getDishId(), cart.getNumber());
                continue;
            }
            if (cart.getSetmealId() != null) {
                List<SetmealDish> dishes = setmealDishMapper.selectBySetmealId(cart.getSetmealId());
                if (CollectionUtils.isEmpty(dishes)) {
                    throw new BusinessException("Setmeal dishes are empty");
                }
                for (SetmealDish item : dishes) {
                    deductDishStock(item.getDishId(), item.getCopies() * cart.getNumber());
                }
            }
        }
    }

    private void deductDishStock(Long dishId, Integer number) {
        if (dishMapper.deductStock(dishId, number) == 0) {
            throw new BusinessException("Dish stock is insufficient");
        }
    }

    @Override
    public Object userPage(Map<String, Object> params) {
        return pageByCurrentUser(params);
    }

    @Override
    public OrderVO detail(Long id) {
        Orders orders = getRequiredOrder(id);
        requireDetailPermission(orders);
        List<OrderDetail> details = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, id));
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(details);
        return orderVO;
    }

    private void requireDetailPermission(Orders orders) {
        if (AuthUtils.isEmployee()) {
            return;
        }
        if (!AuthUtils.ROLE_USER.equals(BaseContext.getCurrentRole())) {
            throw new BusinessException("Permission denied");
        }
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null || !currentId.equals(orders.getUserId())) {
            throw new BusinessException("Order not found");
        }
    }

    @Override
    public Object history(Map<String, Object> params) {
        return pageByCurrentUser(params);
    }

    @Override
    public Page<Orders> pageQuery(Map<String, Object> params) {
        Page<Orders> page = new Page<>(longParam(params, "page", 1L), longParam(params, "pageSize", 10L));
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        Object number = params == null ? null : params.get("number");
        Object phone = params == null ? null : params.get("phone");
        Object status = params == null ? null : params.get("status");
        Object beginTime = params == null ? null : params.get("beginTime");
        Object endTime = params == null ? null : params.get("endTime");
        if (number != null && !String.valueOf(number).isBlank()) {
            wrapper.like(Orders::getNumber, String.valueOf(number).trim());
        }
        if (phone != null && !String.valueOf(phone).isBlank()) {
            wrapper.like(Orders::getPhone, String.valueOf(phone).trim());
        }
        if (status != null && !String.valueOf(status).isBlank()) {
            wrapper.eq(Orders::getStatus, Integer.parseInt(String.valueOf(status)));
        }
        if (beginTime != null && !String.valueOf(beginTime).isBlank()) {
            wrapper.ge(Orders::getOrderTime, parseDateTime(beginTime));
        }
        if (endTime != null && !String.valueOf(endTime).isBlank()) {
            wrapper.le(Orders::getOrderTime, parseDateTime(endTime));
        }
        wrapper.orderByDesc(Orders::getOrderTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Object pageByCurrentUser(Map<String, Object> params) {
        Long userId = requiredCurrentUserId();
        Page<Orders> page = new Page<>(longParam(params, "page", 1L), longParam(params, "pageSize", 10L));
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getOrderTime);
        return page(page, wrapper);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Orders orders = getRequiredOrder(id);
        orders.setStatus(status);
        updateById(orders);
    }

    @Override
    public void cancel(Long id, String reason) {
        Orders orders = getRequiredOrder(id);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(reason);
        orders.setCancelTime(LocalDateTime.now());
        updateById(orders);
    }

    @Override
    public void reject(Long id, String reason) {
        Orders orders = getRequiredOrder(id);
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(reason);
        orders.setCancelTime(LocalDateTime.now());
        updateById(orders);
    }

    @Override
    public void confirm(Long id) {
        updateStatus(id, Orders.CONFIRMED);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = getRequiredOrder(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orders.setDeliveryTime(LocalDateTime.now());
        updateById(orders);
    }

    @Override
    public void complete(Long id) {
        updateStatus(id, Orders.COMPLETED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void repetition(Long id) {
        Long userId = requiredCurrentUserId();
        Orders orders = getRequiredOrder(id);
        if (!userId.equals(orders.getUserId())) {
            throw new BusinessException("Order not found");
        }
        List<OrderDetail> details = orderDetailMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderDetail>()
                        .eq(OrderDetail::getOrderId, id));
        if (CollectionUtils.isEmpty(details)) {
            throw new BusinessException("Order details are empty");
        }
        for (OrderDetail detail : details) {
            shoppingCartService.add(new CartItem(detail.getDishId(), detail.getSetmealId(),
                    detail.getName(), detail.getAmount(), detail.getNumber()));
        }
    }

    @Override
    public void reminder(Long id) {
        getRequiredOrder(id);
    }

    private long longParam(Map<String, Object> params, String name, long defaultValue) {
        if (params == null || params.get(name) == null || String.valueOf(params.get(name)).isBlank()) {
            return defaultValue;
        }
        return Long.parseLong(String.valueOf(params.get(name)));
    }

    private LocalDateTime parseDateTime(Object value) {
        return LocalDateTime.parse(String.valueOf(value), DATE_TIME_FORMATTER);
    }

    private Orders getRequiredOrder(Long id) {
        Orders orders = getById(id);
        if (orders == null) {
            throw new BusinessException("Order not found");
        }
        return orders;
    }

    private OrderDetail toOrderDetail(ShoppingCart cart, Long orderId) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setName(cart.getName());
        orderDetail.setImage(cart.getImage());
        orderDetail.setOrderId(orderId);
        orderDetail.setDishId(cart.getDishId());
        orderDetail.setSetmealId(cart.getSetmealId());
        orderDetail.setDishFlavor(cart.getDishFlavor());
        orderDetail.setNumber(cart.getNumber());
        orderDetail.setAmount(cart.getAmount());
        return orderDetail;
    }

    private String generateOrderNumber(Long userId) {
        return System.currentTimeMillis() + String.format("%06d", userId);
    }

    private String formatAddress(AddressBook addressBook) {
        return nullToEmpty(addressBook.getProvinceName())
                + nullToEmpty(addressBook.getCityName())
                + nullToEmpty(addressBook.getDistrictName())
                + nullToEmpty(addressBook.getDetail());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private Long requiredCurrentUserId() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new BusinessException("Current user is required");
        }
        return currentId;
    }
}
