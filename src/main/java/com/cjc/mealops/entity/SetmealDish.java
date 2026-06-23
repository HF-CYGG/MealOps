package com.cjc.mealops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("setmeal_dish")
public class SetmealDish {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long setmealId;
    private Long dishId;
    private String name;
    private BigDecimal price;
    private Integer copies;
}

