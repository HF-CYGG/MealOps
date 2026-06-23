package com.cjc.mealops.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjc.mealops.dto.DiningTableDTO;
import com.cjc.mealops.entity.DiningTable;
import java.util.List;
import java.util.Map;

public interface DiningTableService extends IService<DiningTable> {
    Object page(Map<String, Object> params);

    DiningTable create(DiningTableDTO dto);

    DiningTable update(DiningTableDTO dto);

    void updateStatus(String status, List<Long> ids);
}
