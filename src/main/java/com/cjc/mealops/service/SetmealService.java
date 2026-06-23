package com.cjc.mealops.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjc.mealops.dto.SetmealDTO;
import com.cjc.mealops.entity.Setmeal;
import com.cjc.mealops.vo.SetmealVO;
import java.util.List;
import java.util.Map;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDTO setmealDTO);

    SetmealVO getWithDish(Long id);

    List<Setmeal> listWithDish(Map<String, Object> params);

    void updateWithDish(SetmealDTO setmealDTO);

    void deleteBatch(List<Long> ids);

    void updateStatus(Integer status, List<Long> ids);

    void startOrStop(Integer status, Long id);
}
