package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.cjc.mealops.entity.Orders;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.mapper.OrdersMapper;
import com.cjc.mealops.mapper.UserMapper;
import com.cjc.mealops.service.impl.StatsServiceImpl;
import com.cjc.mealops.vo.DashboardOverviewVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {
    @Mock
    private OrderDetailMapper orderDetailMapper;
    @Mock
    private OrdersMapper ordersMapper;
    @Mock
    private UserMapper userMapper;

    @Test
    void todayOverviewUsesOrderAndUserAggregates() {
        when(ordersMapper.sumTurnover(any(LocalDateTime.class), any(LocalDateTime.class),
                eq(Orders.PAID), eq(Orders.CANCELLED))).thenReturn(new BigDecimal("5230.00"));
        when(ordersMapper.countValidOrders(any(LocalDateTime.class), any(LocalDateTime.class),
                eq(Orders.PAID), eq(Orders.CANCELLED))).thenReturn(128L);
        when(userMapper.countCreatedBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(32L);
        when(ordersMapper.countByStatus(Orders.TO_BE_CONFIRMED)).thenReturn(12L);
        StatsServiceImpl service = new StatsServiceImpl(orderDetailMapper, ordersMapper, userMapper);

        DashboardOverviewVO overview = service.todayOverview();

        assertThat(overview.todayTurnover()).isEqualByComparingTo("5230.00");
        assertThat(overview.todayValidOrders()).isEqualTo(128L);
        assertThat(overview.newUsers()).isEqualTo(32L);
        assertThat(overview.pendingOrders()).isEqualTo(12L);
    }
}
