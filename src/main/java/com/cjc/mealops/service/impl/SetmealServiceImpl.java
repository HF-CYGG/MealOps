package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.dto.SetmealDTO;
import com.cjc.mealops.entity.Category;
import com.cjc.mealops.entity.Setmeal;
import com.cjc.mealops.entity.SetmealDish;
import com.cjc.mealops.mapper.CategoryMapper;
import com.cjc.mealops.mapper.SetmealDishMapper;
import com.cjc.mealops.mapper.SetmealMapper;
import com.cjc.mealops.service.SetmealService;
import com.cjc.mealops.vo.SetmealVO;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    private final SetmealDishMapper setmealDishMapper;
    private final CategoryMapper categoryMapper;

    public SetmealServiceImpl(SetmealDishMapper setmealDishMapper, CategoryMapper categoryMapper) {
        this.setmealDishMapper = setmealDishMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        fillCreateFields(setmeal);
        save(setmeal);
        saveSetmealDishes(setmeal.getId(), setmealDTO.getSetmealDishes());
    }

    @Override
    public SetmealVO getWithDish(Long id) {
        Setmeal setmeal = getById(id);
        if (setmeal == null) {
            throw new BusinessException("Setmeal not found");
        }
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        Category category = categoryMapper.selectById(setmeal.getCategoryId());
        if (category != null) {
            setmealVO.setCategoryName(category.getName());
        }
        setmealVO.setSetmealDishes(setmealDishMapper.selectBySetmealId(id));
        return setmealVO;
    }

    @Override
    public List<Setmeal> listWithDish(Map<String, Object> params) {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        Object categoryId = params == null ? null : params.get("categoryId");
        Object status = params == null ? null : params.get("status");
        if (categoryId != null && !String.valueOf(categoryId).isBlank()) {
            wrapper.eq(Setmeal::getCategoryId, Long.parseLong(String.valueOf(categoryId)));
        }
        if (status != null && !String.valueOf(status).isBlank()) {
            wrapper.eq(Setmeal::getStatus, Integer.parseInt(String.valueOf(status)));
        }
        wrapper.orderByDesc(Setmeal::getStatus).orderByDesc(Setmeal::getUpdateTime);
        return list(wrapper);
    }

    @Override
    public Page<SetmealVO> pageQuery(Map<String, Object> params) {
        Page<Setmeal> page = new Page<>(longParam(params, "page", 1L), longParam(params, "pageSize", 10L));
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        Object name = params == null ? null : params.get("name");
        Object categoryId = params == null ? null : params.get("categoryId");
        Object status = params == null ? null : params.get("status");
        if (name != null && !String.valueOf(name).isBlank()) {
            wrapper.like(Setmeal::getName, String.valueOf(name).trim());
        }
        if (categoryId != null && !String.valueOf(categoryId).isBlank()) {
            wrapper.eq(Setmeal::getCategoryId, Long.parseLong(String.valueOf(categoryId)));
        }
        if (status != null && !String.valueOf(status).isBlank()) {
            wrapper.eq(Setmeal::getStatus, Integer.parseInt(String.valueOf(status)));
        }
        wrapper.orderByDesc(Setmeal::getStatus).orderByDesc(Setmeal::getUpdateTime);
        Page<Setmeal> setmealPage = baseMapper.selectPage(page, wrapper);
        Map<Long, String> categoryNames = categoryNames(setmealPage.getRecords());
        Page<SetmealVO> result = new Page<>(setmealPage.getCurrent(), setmealPage.getSize(), setmealPage.getTotal());
        result.setRecords(setmealPage.getRecords().stream()
                .map(setmeal -> toSetmealVO(setmeal, categoryNames.get(setmeal.getCategoryId())))
                .toList());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithDish(SetmealDTO setmealDTO) {
        if (setmealDTO.getId() == null) {
            throw new BusinessException("Setmeal id is required");
        }
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(currentUserId());
        updateById(setmeal);
        setmealDishMapper.deleteBySetmealIds(List.of(setmealDTO.getId()));
        saveSetmealDishes(setmealDTO.getId(), setmealDTO.getSetmealDishes());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        long enabledCount = lambdaQuery()
                .in(Setmeal::getId, ids)
                .eq(Setmeal::getStatus, 1)
                .count();
        if (enabledCount > 0) {
            throw new BusinessException("Enabled setmeals cannot be deleted");
        }
        removeByIds(ids);
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(currentUserId());
        if (!updateById(setmeal)) {
            throw new BusinessException("Setmeal not found");
        }
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

    private void saveSetmealDishes(Long setmealId, List<SetmealDish> setmealDishes) {
        if (CollectionUtils.isEmpty(setmealDishes)) {
            return;
        }
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setId(null);
            setmealDish.setSetmealId(setmealId);
            setmealDishMapper.insert(setmealDish);
        }
    }

    private void fillCreateFields(Setmeal setmeal) {
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = currentUserId();
        setmeal.setCreateTime(now);
        setmeal.setUpdateTime(now);
        setmeal.setCreateUser(currentUserId);
        setmeal.setUpdateUser(currentUserId);
        if (setmeal.getStatus() == null) {
            setmeal.setStatus(0);
        }
    }

    private Long currentUserId() {
        Long currentId = BaseContext.getCurrentId();
        return currentId == null ? 0L : currentId;
    }

    private long longParam(Map<String, Object> params, String name, long defaultValue) {
        if (params == null || params.get(name) == null || String.valueOf(params.get(name)).isBlank()) {
            return defaultValue;
        }
        return Long.parseLong(String.valueOf(params.get(name)));
    }

    private Map<Long, String> categoryNames(Collection<Setmeal> setmeals) {
        List<Long> categoryIds = setmeals.stream()
                .map(Setmeal::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (left, right) -> left, HashMap::new));
    }

    private SetmealVO toSetmealVO(Setmeal setmeal, String categoryName) {
        SetmealVO vo = new SetmealVO();
        BeanUtils.copyProperties(setmeal, vo);
        vo.setCategoryName(categoryName);
        return vo;
    }
}
