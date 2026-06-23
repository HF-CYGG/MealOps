package com.cjc.mealops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dining_cart_item")
public class DiningCartItem {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long sessionId;
    private Long creatorUserId;
    private Long dishId;
    private Long setmealId;
    private String dishFlavor;
    private String name;
    private String image;
    private Integer number;
    private BigDecimal amount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
