package com.cjc.mealops.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cjc.mealops.common.R;
import com.cjc.mealops.dto.OrdersSubmitDTO;
import com.cjc.mealops.service.OrderService;
import com.cjc.mealops.vo.OrderVO;
import com.cjc.mealops.vo.OrderSubmitVO;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderControllerTest {

    @Test
    void submitDelegatesToTypedOrderServiceMethod() {
        ApiInvokeSupport api = mock(ApiInvokeSupport.class);
        OrderService orderService = mock(OrderService.class);
        OrdersSubmitDTO request = new OrdersSubmitDTO();
        request.setAddressBookId(3001L);
        request.setPayMethod(1);
        OrderSubmitVO submitted = new OrderSubmitVO(9001L, "202606270001", BigDecimal.valueOf(18), null);
        when(orderService.submit(request)).thenReturn(submitted);

        R<OrderSubmitVO> response = new OrderController(api, orderService).submit(request);

        assertThat(response.getCode()).isEqualTo(1);
        assertThat(response.getData()).isSameAs(submitted);
        verify(orderService).submit(request);
    }

    @Test
    void getByIdDelegatesToOrderDetailServiceMethod() {
        ApiInvokeSupport api = mock(ApiInvokeSupport.class);
        OrderService orderService = mock(OrderService.class);
        OrderVO detail = new OrderVO();
        when(orderService.detail(9001L)).thenReturn(detail);

        R<OrderVO> response = new OrderController(api, orderService).getById(9001L);

        assertThat(response.getCode()).isEqualTo(1);
        assertThat(response.getData()).isSameAs(detail);
        verify(orderService).detail(9001L);
    }
}
