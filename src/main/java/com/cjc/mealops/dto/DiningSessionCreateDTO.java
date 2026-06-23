package com.cjc.mealops.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DiningSessionCreateDTO {
    @NotNull
    private Long tableId;
    @Min(1)
    private Integer partySize;
}
