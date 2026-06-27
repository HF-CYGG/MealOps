package com.cjc.mealops.service.impl;

import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.Orders;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.mapper.OrdersMapper;
import com.cjc.mealops.mapper.UserMapper;
import com.cjc.mealops.service.StatsService;
import com.cjc.mealops.vo.DashboardOverviewVO;
import com.cjc.mealops.vo.DishSalesVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StatsServiceImpl implements StatsService {
    private final OrderDetailMapper orderDetailMapper;
    private final OrdersMapper ordersMapper;
    private final UserMapper userMapper;

    public StatsServiceImpl(OrderDetailMapper orderDetailMapper, OrdersMapper ordersMapper, UserMapper userMapper) {
        this.orderDetailMapper = orderDetailMapper;
        this.ordersMapper = ordersMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<DishSalesVO> hotDishes(LocalDateTime beginTime, LocalDateTime endTime, int limit) {
        if (beginTime == null || endTime == null || beginTime.isAfter(endTime)) {
            throw new BusinessException("Invalid statistics time range");
        }
        int safeLimit = limit <= 0 ? 10 : limit;
        return orderDetailMapper.selectHotDishes(beginTime, endTime, safeLimit);
    }

    @Override
    public DashboardOverviewVO todayOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = today.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay();
        BigDecimal todayTurnover = nonNull(ordersMapper.sumTurnover(
                beginTime, endTime, Orders.PAID, Orders.CANCELLED));
        Long todayValidOrders = nonNull(ordersMapper.countValidOrders(
                beginTime, endTime, Orders.PAID, Orders.CANCELLED));
        Long newUsers = nonNull(userMapper.countCreatedBetween(beginTime, endTime));
        Long pendingOrders = nonNull(ordersMapper.countByStatus(Orders.TO_BE_CONFIRMED));
        return new DashboardOverviewVO(todayTurnover, todayValidOrders, newUsers, pendingOrders);
    }

    private BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long nonNull(Long value) {
        return value == null ? 0L : value;
    }
}
