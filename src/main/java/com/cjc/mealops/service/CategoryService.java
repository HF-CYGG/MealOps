package com.cjc.mealops.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjc.mealops.entity.Category;
import java.util.List;
import java.util.Map;

public interface CategoryService extends IService<Category> {
    List<Category> listByType(Map<String, Object> params);

    void deleteById(Long id);

    void updateStatus(Integer status, Long id);
}
