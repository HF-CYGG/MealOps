package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.entity.DishFlavor;
import com.cjc.mealops.mapper.DishFlavorMapper;
import com.cjc.mealops.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
