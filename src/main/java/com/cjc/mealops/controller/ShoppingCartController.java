package com.cjc.mealops.controller;

import com.cjc.mealops.common.R;
import com.cjc.mealops.dto.ShoppingCartDTO;
import com.cjc.mealops.entity.ShoppingCart;
import com.cjc.mealops.service.ShoppingCartService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        return R.success(shoppingCartService.listCurrentUserCart());
    }

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCartDTO body) {
        return R.success(shoppingCartService.add(body));
    }

    @PostMapping("/sub")
    public R<Void> sub(@RequestBody ShoppingCartDTO body) {
        shoppingCartService.sub(body);
        return R.success(null);
    }

    @DeleteMapping("/clean")
    public R<Void> clean() {
        shoppingCartService.clean();
        return R.success(null);
    }
}
