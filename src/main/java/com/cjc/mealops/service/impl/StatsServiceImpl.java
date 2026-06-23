package com.cjc.mealops.service.impl;

import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.service.StatsService;
import com.cjc.mealops.vo.DishSalesVO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StatsServiceImpl implements StatsService {
    private final OrderDetailMapper orderDetailMapper;

    public StatsServiceImpl(OrderDetailMapper orderDetailMapper) {
        this.orderDetailMapper = orderDetailMapper;
    }

    @Override
    public List<DishSalesVO> hotDishes(LocalDateTime beginTime, LocalDateTime endTime, int limit) {
        if (beginTime == null || endTime == null || beginTime.isAfter(endTime)) {
            throw new BusinessException("Invalid statistics time range");
        }
        int safeLimit = limit <= 0 ? 10 : limit;
        return orderDetailMapper.selectHotDishes(beginTime, endTime, safeLimit);
    }
}
