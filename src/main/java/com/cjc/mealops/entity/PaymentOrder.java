package com.cjc.mealops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("payment_order")
public class PaymentOrder {
    public static final int PENDING = 0;
    public static final int PAID = 1;
    public static final int CANCELLED = 2;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private String paymentNo;
    private BigDecimal amount;
    private Integer payMethod;
    private Integer status;
    private LocalDateTime paidAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
