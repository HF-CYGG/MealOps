package com.cjc.mealops.vo;

import com.cjc.mealops.entity.SetmealDish;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SetmealVO {
    private Long id;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private Integer status;
    private String description;
    private String image;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
    private String categoryName;
    private List<SetmealDish> setmealDishes;
}
