package com.cjc.mealops.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cjc.mealops.common.R;
import com.cjc.mealops.dto.OrdersSubmitDTO;
import com.cjc.mealops.service.OrderService;
import com.cjc.mealops.vo.OrderVO;
import com.cjc.mealops.vo.OrderSubmitVO;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
    void submitBindsStringAddressBookIdAsLong() throws Exception {
        ApiInvokeSupport api = mock(ApiInvokeSupport.class);
        OrderService orderService = mock(OrderService.class);
        when(orderService.submit(any(OrdersSubmitDTO.class)))
                .thenReturn(new OrderSubmitVO(9001L, "202606270001", BigDecimal.valueOf(18), null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(api, orderService)).build();

        mockMvc.perform(post("/order/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "addressBookId": "2070166539388665857",
                                  "payMethod": 1
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<OrdersSubmitDTO> captor = ArgumentCaptor.forClass(OrdersSubmitDTO.class);
        verify(orderService).submit(captor.capture());
        assertThat(captor.getValue().getAddressBookId()).isEqualTo(2070166539388665857L);
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
