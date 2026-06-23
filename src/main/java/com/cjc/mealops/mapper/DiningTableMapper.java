package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.DiningTable;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface DiningTableMapper extends BaseMapper<DiningTable> {
    @Update("""
            update dining_table
            set status = #{newStatus},
                current_session_id = #{sessionId},
                update_time = now()
            where id = #{tableId}
              and status = #{expectedStatus}
            """)
    int occupyIfAvailable(@Param("tableId") Long tableId,
                          @Param("sessionId") Long sessionId,
                          @Param("expectedStatus") String expectedStatus,
                          @Param("newStatus") String newStatus);
}
