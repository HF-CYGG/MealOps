package com.cjc.mealops.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjc.mealops.entity.PaymentOrder;
import com.cjc.mealops.vo.PaymentPrepayVO;
import java.util.Map;

public interface PaymentService extends IService<PaymentOrder> {
    PaymentPrepayVO prepay(Long orderId);

    PaymentOrder getByOrderId(Long orderId);

    PaymentOrder confirm(Long paymentId);

    Object page(Map<String, Object> params);
}
