package com.cjc.mealops.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjc.mealops.dto.DishDTO;
import com.cjc.mealops.entity.Dish;
import com.cjc.mealops.vo.DishVO;
import java.util.List;
import java.util.Map;

public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDTO dishDTO);

    DishVO getWithFlavor(Long id);

    Page<DishVO> pageQuery(Map<String, Object> params);

    List<Dish> listWithFlavor(Map<String, Object> params);

    void updateWithFlavor(DishDTO dishDTO);

    void deleteBatch(List<Long> ids);

    void updateStatus(Integer status, List<Long> ids);

    void startOrStop(Integer status, Long id);
}
