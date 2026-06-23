package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.entity.OrderDetail;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
