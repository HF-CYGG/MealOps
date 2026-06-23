package com.cjc.mealops.dto;

import com.cjc.mealops.entity.SetmealDish;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class SetmealDTO {
    private Long id;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private Integer status;
    private String description;
    private String image;
    private List<SetmealDish> setmealDishes;
}
