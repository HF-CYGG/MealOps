package com.cjc.mealops.service;

import com.cjc.mealops.vo.DishSalesVO;
import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    List<DishSalesVO> hotDishes(LocalDateTime beginTime, LocalDateTime endTime, int limit);
}
