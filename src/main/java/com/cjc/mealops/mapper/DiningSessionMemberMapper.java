package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.DiningSessionMember;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface DiningSessionMemberMapper extends BaseMapper<DiningSessionMember> {
    @Select("""
            select count(1)
            from dining_session_member
            where session_id = #{sessionId}
              and user_id = #{userId}
              and status = 'ACTIVE'
            """)
    long countActiveMember(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    @Select("""
            select count(1)
            from dining_session_member
            where session_id = #{sessionId}
              and status = 'ACTIVE'
            """)
    long countActiveBySessionId(@Param("sessionId") Long sessionId);

    @Update("""
            update dining_session_member
            set status = 'LEFT',
                left_at = now()
            where session_id = #{sessionId}
              and user_id = #{userId}
              and status = 'ACTIVE'
            """)
    int leave(@Param("sessionId") Long sessionId, @Param("userId") Long userId);
}
