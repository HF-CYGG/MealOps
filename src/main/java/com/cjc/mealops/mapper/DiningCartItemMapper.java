package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.DiningCartItem;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DiningCartItemMapper extends BaseMapper<DiningCartItem> {
    @Select("""
            <script>
            select count(1) from dining_cart_item where dish_id in
            <foreach collection="dishIds" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    long countByDishIds(@Param("dishIds") List<Long> dishIds);

    @Select("""
            select *
            from dining_cart_item
            where session_id = #{sessionId}
            order by create_time asc, id asc
            """)
    List<DiningCartItem> selectActiveItems(@Param("sessionId") Long sessionId);

    @Select("""
            select *
            from dining_cart_item
            where session_id = #{sessionId}
              and creator_user_id = #{creatorUserId}
              and ((#{dishId} is not null and dish_id = #{dishId}) or (#{dishId} is null and dish_id is null))
              and ((#{setmealId} is not null and setmeal_id = #{setmealId}) or (#{setmealId} is null and setmeal_id is null))
              and ((#{dishFlavor} is not null and dish_flavor = #{dishFlavor}) or (#{dishFlavor} is null and dish_flavor is null))
            limit 1
            """)
    DiningCartItem findExisting(@Param("sessionId") Long sessionId,
                                @Param("creatorUserId") Long creatorUserId,
                                @Param("dishId") Long dishId,
                                @Param("setmealId") Long setmealId,
                                @Param("dishFlavor") String dishFlavor);

    @Delete("delete from dining_cart_item where session_id = #{sessionId}")
    int deleteBySessionId(@Param("sessionId") Long sessionId);

    @Delete("delete from dining_cart_item where session_id = #{sessionId} and creator_user_id = #{creatorUserId}")
    int deleteBySessionIdAndCreatorUserId(@Param("sessionId") Long sessionId,
                                          @Param("creatorUserId") Long creatorUserId);
}
