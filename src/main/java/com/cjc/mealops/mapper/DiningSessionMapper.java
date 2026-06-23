package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.DiningSession;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DiningSessionMapper extends BaseMapper<DiningSession> {
    @Select("""
            select *
            from dining_session
            where id = #{sessionId}
            for update
            """)
    DiningSession selectForUpdate(@Param("sessionId") Long sessionId);
}
