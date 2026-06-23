package com.cjc.mealops.dto;

import com.cjc.mealops.entity.DishFlavor;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class DishDTO {
    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer status;
    private List<DishFlavor> flavors;
}
