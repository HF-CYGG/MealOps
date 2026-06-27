package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.dto.CartItem;
import com.cjc.mealops.dto.ShoppingCartDTO;
import com.cjc.mealops.entity.Dish;
import com.cjc.mealops.entity.Setmeal;
import com.cjc.mealops.entity.ShoppingCart;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.SetmealMapper;
import com.cjc.mealops.mapper.ShoppingCartMapper;
import com.cjc.mealops.service.ShoppingCartService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
        implements ShoppingCartService {
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    public ShoppingCartServiceImpl(DishMapper dishMapper, SetmealMapper setmealMapper) {
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShoppingCart add(ShoppingCartDTO shoppingCartDTO) {
        Long userId = requiredCurrentUserId();
        ShoppingCart existing = findExisting(userId, shoppingCartDTO.getDishId(),
                shoppingCartDTO.getSetmealId(), shoppingCartDTO.getDishFlavor());
        if (existing != null) {
            existing.setNumber(existing.getNumber() + 1);
            updateById(existing);
            return existing;
        }

        ShoppingCart item = new ShoppingCart();
        item.setUserId(userId);
        item.setDishId(shoppingCartDTO.getDishId());
        item.setSetmealId(shoppingCartDTO.getSetmealId());
        item.setDishFlavor(shoppingCartDTO.getDishFlavor());
        item.setNumber(1);
        item.setCreateTime(LocalDateTime.now());
        fillCatalogSnapshot(item);
        save(item);
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShoppingCart add(CartItem cartItem) {
        Long userId = requiredCurrentUserId();
        int quantity = normalizedQuantity(cartItem.number());
        ShoppingCart existing = findExisting(userId, cartItem.dishId(), cartItem.setmealId(), null);
        if (existing != null) {
            int current = existing.getNumber() == null ? 0 : existing.getNumber();
            existing.setNumber(current + quantity);
            updateById(existing);
            return existing;
        }

        ShoppingCart item = new ShoppingCart();
        item.setUserId(userId);
        item.setDishId(cartItem.dishId());
        item.setSetmealId(cartItem.setmealId());
        item.setName(cartItem.name());
        item.setAmount(cartItem.amount());
        item.setNumber(quantity);
        item.setCreateTime(LocalDateTime.now());
        save(item);
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        Long userId = requiredCurrentUserId();
        ShoppingCart existing = findExisting(userId, shoppingCartDTO.getDishId(),
                shoppingCartDTO.getSetmealId(), shoppingCartDTO.getDishFlavor());
        decrease(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sub(CartItem cartItem) {
        Long userId = requiredCurrentUserId();
        ShoppingCart existing = findExisting(userId, cartItem.dishId(), cartItem.setmealId(), null);
        decrease(existing);
    }

    @Override
    public List<ShoppingCart> listCurrentUserCart() {
        Long userId = requiredCurrentUserId();
        return lambdaQuery()
                .eq(ShoppingCart::getUserId, userId)
                .orderByAsc(ShoppingCart::getCreateTime)
                .list();
    }

    @Override
    public List<ShoppingCart> listCurrentUser() {
        return listCurrentUserCart();
    }

    @Override
    public void clean() {
        Long userId = requiredCurrentUserId();
        lambdaUpdate().eq(ShoppingCart::getUserId, userId).remove();
    }

    private ShoppingCart findExisting(Long userId, Long dishId, Long setmealId, String dishFlavor) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId);
        if (dishId != null) {
            wrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            wrapper.isNull(ShoppingCart::getDishId);
        }
        if (setmealId != null) {
            wrapper.eq(ShoppingCart::getSetmealId, setmealId);
        } else {
            wrapper.isNull(ShoppingCart::getSetmealId);
        }
        if (StringUtils.hasText(dishFlavor)) {
            wrapper.eq(ShoppingCart::getDishFlavor, dishFlavor);
        } else {
            wrapper.and(w -> w.isNull(ShoppingCart::getDishFlavor).or().eq(ShoppingCart::getDishFlavor, ""));
        }
        wrapper.last("limit 1");
        return getOne(wrapper, false);
    }

    private void decrease(ShoppingCart existing) {
        if (existing == null) {
            throw new BusinessException("Shopping cart item not found");
        }
        if (existing.getNumber() <= 1) {
            removeById(existing.getId());
            return;
        }
        existing.setNumber(existing.getNumber() - 1);
        updateById(existing);
    }

    private void fillCatalogSnapshot(ShoppingCart item) {
        if (item.getDishId() != null) {
            Dish dish = dishMapper.selectById(item.getDishId());
            if (dish == null) {
                throw new BusinessException("Dish not found");
            }
            item.setName(dish.getName());
            item.setImage(dish.getImage());
            item.setAmount(dish.getPrice());
            return;
        }
        if (item.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.selectById(item.getSetmealId());
            if (setmeal == null) {
                throw new BusinessException("Setmeal not found");
            }
            item.setName(setmeal.getName());
            item.setImage(setmeal.getImage());
            item.setAmount(setmeal.getPrice());
            return;
        }
        item.setAmount(BigDecimal.ZERO);
        throw new BusinessException("Dish or setmeal id is required");
    }

    private int normalizedQuantity(Integer number) {
        return number == null || number < 1 ? 1 : number;
    }

    private Long requiredCurrentUserId() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new BusinessException("Current user is required");
        }
        return currentId;
    }
}
