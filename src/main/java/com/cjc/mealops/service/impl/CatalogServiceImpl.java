package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.Dish;
import com.cjc.mealops.entity.Setmeal;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.SetmealMapper;
import com.cjc.mealops.service.CatalogService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CatalogServiceImpl implements CatalogService {
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    public CatalogServiceImpl(DishMapper dishMapper, SetmealMapper setmealMapper) {
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
    }

    @Override
    public List<Map<String, Object>> search(Map<String, Object> params) {
        String keyword = params == null || params.get("keyword") == null ? null : String.valueOf(params.get("keyword"));
        String type = params == null || params.get("type") == null ? null : String.valueOf(params.get("type"));
        List<Map<String, Object>> result = new ArrayList<>();
        if (type == null || type.isBlank() || "dish".equalsIgnoreCase(type)) {
            LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, 1);
            if (keyword != null && !keyword.isBlank()) {
                wrapper.like(Dish::getName, keyword);
            }
            dishMapper.selectList(wrapper).forEach(item -> result.add(dishItem(item)));
        }
        if (type == null || type.isBlank() || "setmeal".equalsIgnoreCase(type)) {
            LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getStatus, 1);
            if (keyword != null && !keyword.isBlank()) {
                wrapper.like(Setmeal::getName, keyword);
            }
            setmealMapper.selectList(wrapper).forEach(item -> result.add(setmealItem(item)));
        }
        return result;
    }

    @Override
    public Map<String, Object> getItem(String type, Long id) {
        if ("dish".equalsIgnoreCase(type)) {
            Dish dish = dishMapper.selectById(id);
            if (dish == null) {
                throw new BusinessException("Catalog item not found");
            }
            return dishItem(dish);
        }
        if ("setmeal".equalsIgnoreCase(type)) {
            Setmeal setmeal = setmealMapper.selectById(id);
            if (setmeal == null) {
                throw new BusinessException("Catalog item not found");
            }
            return setmealItem(setmeal);
        }
        throw new BusinessException("Catalog type is invalid");
    }

    private Map<String, Object> dishItem(Dish dish) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", "dish");
        item.put("id", dish.getId());
        item.put("name", dish.getName());
        item.put("price", dish.getPrice());
        item.put("image", dish.getImage());
        item.put("description", dish.getDescription());
        item.put("categoryId", dish.getCategoryId());
        item.put("stock", dish.getStock());
        return item;
    }

    private Map<String, Object> setmealItem(Setmeal setmeal) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", "setmeal");
        item.put("id", setmeal.getId());
        item.put("name", setmeal.getName());
        item.put("price", setmeal.getPrice());
        item.put("image", setmeal.getImage());
        item.put("description", setmeal.getDescription());
        item.put("categoryId", setmeal.getCategoryId());
        return item;
    }
}
