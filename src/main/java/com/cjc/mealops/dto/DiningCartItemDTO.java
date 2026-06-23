package com.cjc.mealops.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class DiningCartItemDTO {
    private Long dishId;
    private Long setmealId;
    private String dishFlavor;
    @Min(1)
    private Integer number;
}
