package com.cjc.mealops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("orders")
public class Orders {
    public static final int PENDING_PAYMENT = 1;
    public static final int TO_BE_CONFIRMED = 2;
    public static final int CONFIRMED = 3;
    public static final int DELIVERY_IN_PROGRESS = 4;
    public static final int COMPLETED = 5;
    public static final int CANCELLED = 6;

    public static final int UN_PAID = 0;
    public static final int PAID = 1;
    public static final String TYPE_TAKEOUT = "TAKEOUT";
    public static final String TYPE_DINE_IN = "DINE_IN";

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String number;
    private Integer status;
    private String orderType;
    private Long userId;
    private Long diningSessionId;
    private Long tableId;
    private String tableName;
    private Integer partySize;
    private Long addressBookId;
    private LocalDateTime orderTime;
    private LocalDateTime checkoutTime;
    private Integer payMethod;
    private Integer payStatus;
    private BigDecimal amount;
    private String remark;
    private String phone;
    private String address;
    private String userName;
    private String consignee;
    private String cancelReason;
    private String rejectionReason;
    private LocalDateTime cancelTime;
    private LocalDateTime estimatedDeliveryTime;
    private Integer deliveryStatus;
    private LocalDateTime deliveryTime;
    private Integer packAmount;
    private Integer tablewareNumber;
    private Integer tablewareStatus;
}

