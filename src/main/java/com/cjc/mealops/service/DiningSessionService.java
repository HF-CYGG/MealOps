package com.cjc.mealops.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjc.mealops.dto.DiningCartItemDTO;
import com.cjc.mealops.dto.DiningSessionCreateDTO;
import com.cjc.mealops.entity.DiningCartItem;
import com.cjc.mealops.entity.DiningSession;
import com.cjc.mealops.vo.DiningOrderSubmitVO;
import java.util.List;
import java.util.Map;

public interface DiningSessionService extends IService<DiningSession> {
    DiningSession createSession(DiningSessionCreateDTO dto);

    Map<String, Object> detail(Long sessionId);

    DiningSession join(Long sessionId);

    DiningSession updatePartySize(Long sessionId, Integer partySize);

    void leave(Long sessionId);

    List<DiningCartItem> cart(Long sessionId);

    DiningCartItem addCartItem(Long sessionId, DiningCartItemDTO dto);

    DiningCartItem updateCartItem(Long sessionId, Long itemId, Integer number);

    void deleteCartItem(Long sessionId, Long itemId);

    void clearCart(Long sessionId);

    DiningOrderSubmitVO submitOrder(Long sessionId);

    List<?> orderedItems(Long sessionId);

    Object page(Map<String, Object> params);

    void forceClose(Long sessionId);
}
