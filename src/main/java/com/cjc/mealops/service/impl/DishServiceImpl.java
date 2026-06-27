package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.dto.DishDTO;
import com.cjc.mealops.entity.Category;
import com.cjc.mealops.entity.Dish;
import com.cjc.mealops.entity.DishFlavor;
import com.cjc.mealops.mapper.CategoryMapper;
import com.cjc.mealops.mapper.DiningCartItemMapper;
import com.cjc.mealops.mapper.DishFlavorMapper;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.mapper.SetmealDishMapper;
import com.cjc.mealops.mapper.ShoppingCartMapper;
import com.cjc.mealops.service.DishService;
import com.cjc.mealops.vo.DishVO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    private final DishFlavorMapper dishFlavorMapper;
    private final CategoryMapper categoryMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final DiningCartItemMapper diningCartItemMapper;
    private final OrderDetailMapper orderDetailMapper;

    public DishServiceImpl(DishFlavorMapper dishFlavorMapper,
                           CategoryMapper categoryMapper,
                           SetmealDishMapper setmealDishMapper,
                           ShoppingCartMapper shoppingCartMapper,
                           DiningCartItemMapper diningCartItemMapper,
                           OrderDetailMapper orderDetailMapper) {
        this.dishFlavorMapper = dishFlavorMapper;
        this.categoryMapper = categoryMapper;
        this.setmealDishMapper = setmealDishMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.diningCartItemMapper = diningCartItemMapper;
        this.orderDetailMapper = orderDetailMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        fillCreateFields(dish);
        save(dish);
        saveFlavors(dish.getId(), dishDTO.getFlavors());
    }

    @Override
    public DishVO getWithFlavor(Long id) {
        Dish dish = getById(id);
        if (dish == null) {
            throw new BusinessException("Dish not found");
        }
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        Category category = categoryMapper.selectById(dish.getCategoryId());
        if (category != null) {
            dishVO.setCategoryName(category.getName());
        }
        dishVO.setFlavors(dishFlavorMapper.selectByDishId(id));
        return dishVO;
    }

    @Override
    public List<Dish> listWithFlavor(Map<String, Object> params) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        Object categoryId = params == null ? null : params.get("categoryId");
        Object status = params == null ? null : params.get("status");
        if (categoryId != null && !String.valueOf(categoryId).isBlank()) {
            wrapper.eq(Dish::getCategoryId, Long.parseLong(String.valueOf(categoryId)));
        }
        if (status != null && !String.valueOf(status).isBlank()) {
            wrapper.eq(Dish::getStatus, Integer.parseInt(String.valueOf(status)));
        }
        wrapper.orderByDesc(Dish::getUpdateTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithFlavor(DishDTO dishDTO) {
        if (dishDTO.getId() == null) {
            throw new BusinessException("Dish id is required");
        }
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(currentUserId());
        updateById(dish);
        dishFlavorMapper.deleteByDishIds(List.of(dishDTO.getId()));
        saveFlavors(dishDTO.getId(), dishDTO.getFlavors());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        long enabledCount = baseMapper.selectCount(new LambdaQueryWrapper<Dish>()
                .in(Dish::getId, ids)
                .eq(Dish::getStatus, 1));
        if (enabledCount > 0) {
            throw new BusinessException("Enabled dishes cannot be deleted");
        }
        if (setmealDishMapper.countByDishIds(ids) > 0) {
            throw new BusinessException("Dish is referenced by set meal and cannot be deleted");
        }
        if (shoppingCartMapper.countByDishIds(ids) > 0) {
            throw new BusinessException("Dish is referenced by shopping cart and cannot be deleted");
        }
        if (diningCartItemMapper.countByDishIds(ids) > 0) {
            throw new BusinessException("Dish is referenced by dining cart and cannot be deleted");
        }
        if (orderDetailMapper.countByDishIds(ids) > 0) {
            throw new BusinessException("Dish is referenced by order detail and cannot be deleted");
        }
        removeByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(currentUserId());
        updateById(dish);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer status, List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            startOrStop(status, id);
        }
    }

    private void saveFlavors(Long dishId, List<DishFlavor> flavors) {
        if (CollectionUtils.isEmpty(flavors)) {
            return;
        }
        for (DishFlavor flavor : flavors) {
            flavor.setId(null);
            flavor.setDishId(dishId);
            dishFlavorMapper.insert(flavor);
        }
    }

    private void fillCreateFields(Dish dish) {
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = currentUserId();
        dish.setCreateTime(now);
        dish.setUpdateTime(now);
        dish.setCreateUser(currentUserId);
        dish.setUpdateUser(currentUserId);
        if (dish.getStatus() == null) {
            dish.setStatus(0);
        }
    }

    private Long currentUserId() {
        Long currentId = BaseContext.getCurrentId();
        return currentId == null ? 0L : currentId;
    }
}
