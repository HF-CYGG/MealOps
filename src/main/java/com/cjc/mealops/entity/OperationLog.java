package com.cjc.mealops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long operationUser;
    private String operationType;
    private String operationMethod;
    private String operationParams;
    private String operationResult;
    private LocalDateTime operationTime;
    private Long duration;
    private String ip;
}

