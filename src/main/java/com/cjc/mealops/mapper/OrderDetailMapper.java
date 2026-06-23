package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.OrderDetail;
import com.cjc.mealops.vo.DishSalesVO;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
    @Select("""
            select od.name as name, coalesce(sum(od.number), 0) as number
            from order_detail od
            inner join orders o on o.id = od.order_id
            where od.dish_id is not null
              and o.status = 5
              and o.order_time between #{beginTime} and #{endTime}
            group by od.name
            order by number desc
            limit #{limit}
            """)
    List<DishSalesVO> selectHotDishes(@Param("beginTime") LocalDateTime beginTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("limit") int limit);
}
