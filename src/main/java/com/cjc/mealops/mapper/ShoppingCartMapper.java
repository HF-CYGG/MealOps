package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.ShoppingCart;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
    @Select("""
            <script>
            select count(1) from shopping_cart where dish_id in
            <foreach collection="dishIds" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    long countByDishIds(@Param("dishIds") List<Long> dishIds);
}
