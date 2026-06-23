package com.cjc.mealops.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrdersSubmitDTO {
    private Long addressBookId;
    private Integer payMethod;
    private String remark;
    private LocalDateTime estimatedDeliveryTime;
    private Integer deliveryStatus;
    private Integer packAmount;
    private Integer tablewareNumber;
    private Integer tablewareStatus;
}
