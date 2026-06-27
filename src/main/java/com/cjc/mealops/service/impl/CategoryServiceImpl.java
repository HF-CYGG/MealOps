package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.Category;
import com.cjc.mealops.mapper.CategoryMapper;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.SetmealMapper;
import com.cjc.mealops.service.CategoryDeletionPolicy;
import com.cjc.mealops.service.CategoryService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    private final CategoryDeletionPolicy categoryDeletionPolicy;

    public CategoryServiceImpl(DishMapper dishMapper,
                               SetmealMapper setmealMapper,
                               CategoryDeletionPolicy categoryDeletionPolicy) {
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
        this.categoryDeletionPolicy = categoryDeletionPolicy;
    }

    @Override
    public List<Category> listByType(Map<String, Object> params) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        Object type = params == null ? null : params.get("type");
        if (type != null && !String.valueOf(type).isBlank()) {
            wrapper.eq(Category::getType, Integer.parseInt(String.valueOf(type)));
        }
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException("Category not found");
        }
        long dishCount = dishMapper.countByCategoryId(id);
        long setmealCount = setmealMapper.countByCategoryId(id);
        categoryDeletionPolicy.check(category.getType(), dishCount, setmealCount);
        removeById(id);
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        if (!updateById(category)) {
            throw new BusinessException("Category not found");
        }
    }
}
