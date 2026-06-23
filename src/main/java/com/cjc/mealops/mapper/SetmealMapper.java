package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.Setmeal;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SetmealMapper extends BaseMapper<Setmeal> {
    @Select("select count(1) from setmeal where category_id = #{categoryId}")
    long countByCategoryId(@Param("categoryId") Long categoryId);
}
