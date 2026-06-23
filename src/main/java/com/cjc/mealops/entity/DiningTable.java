package com.cjc.mealops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dining_table")
public class DiningTable {
    public static final String AVAILABLE = "AVAILABLE";
    public static final String OCCUPIED = "OCCUPIED";
    public static final String RESERVED = "RESERVED";
    public static final String DISABLED = "DISABLED";

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String tableNo;
    private String tableName;
    private Integer capacity;
    private String status;
    private Long currentSessionId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
