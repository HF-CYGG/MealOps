package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.User;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {
    @Select("""
            select count(1)
            from user
            where create_time >= #{beginTime}
              and create_time < #{endTime}
            """)
    Long countCreatedBetween(@Param("beginTime") LocalDateTime beginTime,
                             @Param("endTime") LocalDateTime endTime);
}
