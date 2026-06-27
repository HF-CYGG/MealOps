package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.Orders;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface OrdersMapper extends BaseMapper<Orders> {
    @Select("""
            select coalesce(sum(amount), 0)
            from orders
            where order_time >= #{beginTime}
              and order_time < #{endTime}
              and pay_status = #{payStatus}
              and status <> #{cancelledStatus}
            """)
    BigDecimal sumTurnover(@Param("beginTime") LocalDateTime beginTime,
                           @Param("endTime") LocalDateTime endTime,
                           @Param("payStatus") Integer payStatus,
                           @Param("cancelledStatus") Integer cancelledStatus);

    @Select("""
            select count(1)
            from orders
            where order_time >= #{beginTime}
              and order_time < #{endTime}
              and pay_status = #{payStatus}
              and status <> #{cancelledStatus}
            """)
    Long countValidOrders(@Param("beginTime") LocalDateTime beginTime,
                          @Param("endTime") LocalDateTime endTime,
                          @Param("payStatus") Integer payStatus,
                          @Param("cancelledStatus") Integer cancelledStatus);

    @Select("select count(1) from orders where status = #{status}")
    Long countByStatus(@Param("status") Integer status);

    @Update("""
            update orders
            set address_book_id = null
            where user_id = #{userId}
              and address_book_id = #{addressBookId}
            """)
    int clearAddressBookReference(@Param("userId") Long userId,
                                  @Param("addressBookId") Long addressBookId);
}
