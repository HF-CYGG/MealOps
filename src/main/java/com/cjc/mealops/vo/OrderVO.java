package com.cjc.mealops.vo;

import com.cjc.mealops.entity.OrderDetail;
import com.cjc.mealops.entity.Orders;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Orders {
    private List<OrderDetail> orderDetailList;
}
