package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.DishFlavor;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DishFlavorMapper extends BaseMapper<DishFlavor> {
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> selectByDishId(@Param("dishId") Long dishId);

    @Delete("""
            <script>
            delete from dish_flavor where dish_id in
            <foreach collection="dishIds" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    int deleteByDishIds(@Param("dishIds") List<Long> dishIds);
}
