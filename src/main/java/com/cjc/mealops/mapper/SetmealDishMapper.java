package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.SetmealDish;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> selectBySetmealId(@Param("setmealId") Long setmealId);

    @Select("""
            <script>
            select count(1) from setmeal_dish where dish_id in
            <foreach collection="dishIds" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    long countByDishIds(@Param("dishIds") List<Long> dishIds);

    @Delete("""
            <script>
            delete from setmeal_dish where setmeal_id in
            <foreach collection="setmealIds" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    int deleteBySetmealIds(@Param("setmealIds") List<Long> setmealIds);
}
