package com.cjc.mealops.dto;

import lombok.Data;

@Data
public class OrdersStatusDTO {
    private Long id;
    private Integer status;
    private String reason;
}
