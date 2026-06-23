package com.cjc.mealops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dining_session")
public class DiningSession {
    public static final String ACTIVE = "ACTIVE";
    public static final String CLOSED = "CLOSED";

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tableId;
    private String tableName;
    private Long creatorUserId;
    private Integer partySize;
    private String status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
