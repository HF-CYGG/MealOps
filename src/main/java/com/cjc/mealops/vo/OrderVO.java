package com.cjc.mealops.vo;

import com.cjc.mealops.entity.OrderDetail;
import com.cjc.mealops.entity.Orders;
import java.util.List;
import lombok.Data;

@Data
public class OrderVO {
    private Orders order;
    private List<OrderDetail> orderDetailList;
}
