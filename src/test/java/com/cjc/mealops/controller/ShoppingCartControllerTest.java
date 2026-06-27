package com.cjc.mealops.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cjc.mealops.common.R;
import com.cjc.mealops.dto.ShoppingCartDTO;
import com.cjc.mealops.entity.ShoppingCart;
import com.cjc.mealops.service.ShoppingCartService;
import java.util.List;
import org.junit.jupiter.api.Test;

class ShoppingCartControllerTest {

    @Test
    void addDelegatesToShoppingCartDtoServiceMethod() {
        ShoppingCartService service = mock(ShoppingCartService.class);
        ShoppingCartDTO request = new ShoppingCartDTO();
        request.setDishId(1001L);
        ShoppingCart saved = new ShoppingCart();
        saved.setDishId(1001L);
        saved.setNumber(1);
        when(service.add(request)).thenReturn(saved);

        R<ShoppingCart> response = new ShoppingCartController(service).add(request);

        assertThat(response.getCode()).isEqualTo(1);
        assertThat(response.getData()).isSameAs(saved);
        verify(service).add(request);
    }

    @Test
    void subDelegatesToShoppingCartDtoServiceMethod() {
        ShoppingCartService service = mock(ShoppingCartService.class);
        ShoppingCartDTO request = new ShoppingCartDTO();
        request.setSetmealId(2001L);

        R<Void> response = new ShoppingCartController(service).sub(request);

        assertThat(response.getCode()).isEqualTo(1);
        verify(service).sub(request);
    }

    @Test
    void listUsesCurrentUserCart() {
        ShoppingCartService service = mock(ShoppingCartService.class);
        ShoppingCart item = new ShoppingCart();
        item.setName("番茄炒蛋");
        when(service.listCurrentUserCart()).thenReturn(List.of(item));

        R<List<ShoppingCart>> response = new ShoppingCartController(service).list();

        assertThat(response.getCode()).isEqualTo(1);
        assertThat(response.getData()).containsExactly(item);
        verify(service).listCurrentUserCart();
    }

    @Test
    void cleanUsesCurrentUserCart() {
        ShoppingCartService service = mock(ShoppingCartService.class);

        R<Void> response = new ShoppingCartController(service).clean();

        assertThat(response.getCode()).isEqualTo(1);
        verify(service).clean();
    }
}
