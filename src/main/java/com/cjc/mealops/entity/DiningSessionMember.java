package com.cjc.mealops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dining_session_member")
public class DiningSessionMember {
    public static final String ACTIVE = "ACTIVE";
    public static final String LEFT = "LEFT";

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long sessionId;
    private Long userId;
    private String nickname;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
}
