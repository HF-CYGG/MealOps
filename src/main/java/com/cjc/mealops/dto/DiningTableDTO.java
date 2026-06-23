package com.cjc.mealops.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DiningTableDTO {
    private Long id;
    @NotBlank
    private String tableNo;
    private String tableName;
    @Min(1)
    private Integer capacity;
    private String status;
}
