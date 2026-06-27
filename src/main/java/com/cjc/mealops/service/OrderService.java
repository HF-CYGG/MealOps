package com.cjc.mealops.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjc.mealops.dto.OrdersSubmitDTO;
import com.cjc.mealops.entity.Orders;
import com.cjc.mealops.vo.OrderVO;
import com.cjc.mealops.vo.OrderSubmitVO;
import java.util.Map;

public interface OrderService extends IService<Orders> {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    OrderVO detail(Long id);

    Object userPage(Map<String, Object> params);

    Object history(Map<String, Object> params);

    Object pageByCurrentUser(Map<String, Object> params);

    void updateStatus(Long id, Integer status);

    void cancel(Long id, String reason);

    void reject(Long id, String reason);

    void confirm(Long id);

    void delivery(Long id);

    void complete(Long id);

    void repetition(Long id);

    void reminder(Long id);
}
