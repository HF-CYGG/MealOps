package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.util.Map;
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

    @Test
    void pageQueryFiltersOrdersBySearchFormFields() {
        OrderServiceImpl service = serviceWithOrder(ownedOrder());
        when(ordersMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<Orders> page = invocation.getArgument(0);
            page.setRecords(List.of(ownedOrder()));
            page.setTotal(1);
            return page;
        });

        Page<Orders> result = service.pageQuery(Map.of(
                "page", "3",
                "pageSize", "20",
                "number", "20260627",
                "phone", "139",
                "status", "2",
                "beginTime", "2026-06-27 00:00:00",
                "endTime", "2026-06-27 23:59:59"));

        assertThat(result.getCurrent()).isEqualTo(3);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotal()).isEqualTo(1);
        verify(ordersMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void summaryReturnsCountsForEveryOrderStatus() {
        OrderServiceImpl service = serviceWithOrder(ownedOrder());
        when(ordersMapper.countByStatus(Orders.PENDING_PAYMENT)).thenReturn(2L);
        when(ordersMapper.countByStatus(Orders.TO_BE_CONFIRMED)).thenReturn(3L);
        when(ordersMapper.countByStatus(Orders.CONFIRMED)).thenReturn(4L);
        when(ordersMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS)).thenReturn(5L);
        when(ordersMapper.countByStatus(Orders.COMPLETED)).thenReturn(6L);
        when(ordersMapper.countByStatus(Orders.CANCELLED)).thenReturn(7L);

        Map<String, Long> result = service.summary();

        assertThat(result).containsEntry("pendingPayment", 2L)
                .containsEntry("toBeConfirmed", 3L)
                .containsEntry("preparing", 4L)
                .containsEntry("serving", 5L)
                .containsEntry("completed", 6L)
                .containsEntry("cancelled", 7L);
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
