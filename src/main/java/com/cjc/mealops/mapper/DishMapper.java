package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.Dish;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface DishMapper extends BaseMapper<Dish> {
    @Select("select count(1) from dish where category_id = #{categoryId}")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    @Update("""
            update dish
            set stock = stock - #{number},
                version = version + 1
            where id = #{dishId}
              and stock >= #{number}
            """)
    int deductStock(@Param("dishId") Long dishId, @Param("number") Integer number);

    @Update("""
            update dish
            set stock = stock + #{number},
                version = version + 1
            where id = #{dishId}
            """)
    int restoreStock(@Param("dishId") Long dishId, @Param("number") Integer number);
}
