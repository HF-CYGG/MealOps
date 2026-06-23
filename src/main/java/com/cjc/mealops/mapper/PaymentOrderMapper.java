package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.PaymentOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {
    @Select("""
            select *
            from payment_order
            where order_id = #{orderId}
            order by create_time desc, id desc
            limit 1
            """)
    PaymentOrder selectLatestByOrderId(@Param("orderId") Long orderId);

    @Update("""
            update payment_order
            set status = #{paidStatus},
                paid_at = now(),
                update_time = now()
            where id = #{paymentId}
              and status = #{pendingStatus}
            """)
    int markPendingPaid(@Param("paymentId") Long paymentId,
                        @Param("pendingStatus") int pendingStatus,
                        @Param("paidStatus") int paidStatus);
}
