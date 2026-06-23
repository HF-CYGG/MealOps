package com.cjc.mealops.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjc.mealops.dto.CartItem;
import com.cjc.mealops.dto.ShoppingCartDTO;
import com.cjc.mealops.entity.ShoppingCart;
import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {
    ShoppingCart add(ShoppingCartDTO shoppingCartDTO);

    ShoppingCart add(CartItem cartItem);

    void sub(ShoppingCartDTO shoppingCartDTO);

    void sub(CartItem cartItem);

    List<ShoppingCart> listCurrentUserCart();

    List<ShoppingCart> listCurrentUser();

    void clean();
}
