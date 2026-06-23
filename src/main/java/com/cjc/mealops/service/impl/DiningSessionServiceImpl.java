package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.common.AuthUtils;
import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.dto.DiningCartItemDTO;
import com.cjc.mealops.dto.DiningSessionCreateDTO;
import com.cjc.mealops.entity.DiningCartItem;
import com.cjc.mealops.entity.DiningSession;
import com.cjc.mealops.entity.DiningSessionMember;
import com.cjc.mealops.entity.DiningTable;
import com.cjc.mealops.entity.Dish;
import com.cjc.mealops.entity.OrderDetail;
import com.cjc.mealops.entity.Orders;
import com.cjc.mealops.entity.Setmeal;
import com.cjc.mealops.entity.SetmealDish;
import com.cjc.mealops.mapper.DiningCartItemMapper;
import com.cjc.mealops.mapper.DiningSessionMapper;
import com.cjc.mealops.mapper.DiningSessionMemberMapper;
import com.cjc.mealops.mapper.DiningTableMapper;
import com.cjc.mealops.mapper.DishMapper;
import com.cjc.mealops.mapper.OrderDetailMapper;
import com.cjc.mealops.mapper.OrdersMapper;
import com.cjc.mealops.mapper.SetmealDishMapper;
import com.cjc.mealops.mapper.SetmealMapper;
import com.cjc.mealops.service.DiningSessionService;
import com.cjc.mealops.vo.DiningOrderSubmitVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class DiningSessionServiceImpl extends ServiceImpl<DiningSessionMapper, DiningSession>
        implements DiningSessionService {
    private final DiningSessionMapper diningSessionMapper;
    private final DiningSessionMemberMapper diningSessionMemberMapper;
    private final DiningTableMapper diningTableMapper;
    private final DiningCartItemMapper diningCartItemMapper;
    private final OrdersMapper ordersMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;

    public DiningSessionServiceImpl(DiningSessionMapper diningSessionMapper,
                                    DiningSessionMemberMapper diningSessionMemberMapper,
                                    DiningTableMapper diningTableMapper,
                                    DiningCartItemMapper diningCartItemMapper,
                                    OrdersMapper ordersMapper,
                                    OrderDetailMapper orderDetailMapper,
                                    DishMapper dishMapper,
                                    SetmealMapper setmealMapper,
                                    SetmealDishMapper setmealDishMapper) {
        this.diningSessionMapper = diningSessionMapper;
        this.diningSessionMemberMapper = diningSessionMemberMapper;
        this.diningTableMapper = diningTableMapper;
        this.diningCartItemMapper = diningCartItemMapper;
        this.ordersMapper = ordersMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
        this.setmealDishMapper = setmealDishMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningSession createSession(DiningSessionCreateDTO dto) {
        Long userId = requiredCurrentUserId();
        DiningTable table = diningTableMapper.selectById(dto.getTableId());
        if (table == null) {
            throw new BusinessException("Dining table not found");
        }
        if (!DiningTable.AVAILABLE.equals(table.getStatus())) {
            throw new BusinessException("Dining table is not available");
        }

        LocalDateTime now = LocalDateTime.now();
        DiningSession session = new DiningSession();
        session.setId(IdWorker.getId());
        session.setTableId(table.getId());
        session.setTableName(table.getTableName());
        session.setCreatorUserId(userId);
        session.setPartySize(defaultPartySize(dto.getPartySize()));
        session.setStatus(DiningSession.ACTIVE);
        session.setOpenedAt(now);
        session.setCreateTime(now);
        session.setUpdateTime(now);
        diningSessionMapper.insert(session);

        DiningSessionMember member = new DiningSessionMember();
        member.setSessionId(session.getId());
        member.setUserId(userId);
        member.setStatus(DiningSessionMember.ACTIVE);
        member.setJoinedAt(now);
        diningSessionMemberMapper.insert(member);

        if (diningTableMapper.occupyIfAvailable(
                table.getId(), session.getId(), DiningTable.AVAILABLE, DiningTable.OCCUPIED) == 0) {
            throw new BusinessException("Dining table is not available");
        }
        return session;
    }

    @Override
    public Map<String, Object> detail(Long sessionId) {
        DiningSession session = requiredSession(sessionId);
        if (!AuthUtils.isEmployee()) {
            requireActiveMember(sessionId);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("session", session);
        result.put("cart", diningCartItemMapper.selectActiveItems(sessionId));
        result.put("orderedItems", orderedItems(sessionId));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningSession join(Long sessionId) {
        Long userId = requiredCurrentUserId();
        DiningSession session = requiredActiveSession(sessionId);
        if (diningSessionMemberMapper.countActiveMember(sessionId, userId) > 0) {
            return session;
        }
        DiningSessionMember member = new DiningSessionMember();
        member.setSessionId(sessionId);
        member.setUserId(userId);
        member.setStatus(DiningSessionMember.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());
        diningSessionMemberMapper.insert(member);
        return session;
    }

    @Override
    public DiningSession updatePartySize(Long sessionId, Integer partySize) {
        requireActiveMember(sessionId);
        DiningSession session = requiredActiveSession(sessionId);
        session.setPartySize(defaultPartySize(partySize));
        session.setUpdateTime(LocalDateTime.now());
        diningSessionMapper.updateById(session);
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leave(Long sessionId) {
        Long userId = requiredCurrentUserId();
        requiredActiveSession(sessionId);
        diningSessionMemberMapper.leave(sessionId, userId);
        if (diningSessionMemberMapper.countActiveBySessionId(sessionId) == 0) {
            forceClose(sessionId);
        }
    }

    @Override
    public List<DiningCartItem> cart(Long sessionId) {
        requireActiveMember(sessionId);
        return diningCartItemMapper.selectActiveItems(sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningCartItem addCartItem(Long sessionId, DiningCartItemDTO dto) {
        Long userId = requireActiveMember(sessionId);
        int number = defaultNumber(dto.getNumber());
        DiningCartItem existing = diningCartItemMapper.findExisting(sessionId, userId,
                dto.getDishId(), dto.getSetmealId(), blankToNull(dto.getDishFlavor()));
        if (existing != null) {
            existing.setNumber(existing.getNumber() + number);
            existing.setUpdateTime(LocalDateTime.now());
            diningCartItemMapper.updateById(existing);
            return existing;
        }

        DiningCartItem item = new DiningCartItem();
        item.setSessionId(sessionId);
        item.setCreatorUserId(userId);
        item.setDishId(dto.getDishId());
        item.setSetmealId(dto.getSetmealId());
        item.setDishFlavor(blankToNull(dto.getDishFlavor()));
        item.setNumber(number);
        LocalDateTime now = LocalDateTime.now();
        item.setCreateTime(now);
        item.setUpdateTime(now);
        fillCatalogSnapshot(item);
        diningCartItemMapper.insert(item);
        return item;
    }

    @Override
    public DiningCartItem updateCartItem(Long sessionId, Long itemId, Integer number) {
        Long userId = requireActiveMember(sessionId);
        DiningCartItem item = requiredCartItem(sessionId, itemId);
        requireCartItemCreator(item, userId);
        item.setNumber(defaultNumber(number));
        item.setUpdateTime(LocalDateTime.now());
        diningCartItemMapper.updateById(item);
        return item;
    }

    @Override
    public void deleteCartItem(Long sessionId, Long itemId) {
        Long userId = requireActiveMember(sessionId);
        DiningCartItem item = requiredCartItem(sessionId, itemId);
        requireCartItemCreator(item, userId);
        diningCartItemMapper.deleteById(itemId);
    }

    @Override
    public void clearCart(Long sessionId) {
        Long userId = requireActiveMember(sessionId);
        diningCartItemMapper.deleteBySessionIdAndCreatorUserId(sessionId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningOrderSubmitVO submitOrder(Long sessionId) {
        Long userId = requireActiveMember(sessionId);
        DiningSession session = diningSessionMapper.selectForUpdate(sessionId);
        if (session == null || !DiningSession.ACTIVE.equals(session.getStatus())) {
            throw new BusinessException("Dining session is closed");
        }
        List<DiningCartItem> items = diningCartItemMapper.selectActiveItems(sessionId);
        if (CollectionUtils.isEmpty(items)) {
            throw new BusinessException("Dining cart is empty");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (DiningCartItem item : items) {
            fillCatalogSnapshot(item);
            deductStock(item);
            total = total.add(item.getAmount().multiply(BigDecimal.valueOf(item.getNumber())));
        }

        LocalDateTime now = LocalDateTime.now();
        Orders order = new Orders();
        order.setId(IdWorker.getId());
        order.setNumber(generateOrderNumber(userId));
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setOrderType(Orders.TYPE_DINE_IN);
        order.setUserId(userId);
        order.setDiningSessionId(sessionId);
        order.setTableId(session.getTableId());
        order.setTableName(session.getTableName());
        order.setPartySize(session.getPartySize());
        order.setOrderTime(now);
        order.setPayMethod(1);
        order.setPayStatus(Orders.UN_PAID);
        order.setAmount(total);
        order.setPhone("");
        order.setAddress("堂食-" + nullToEmpty(session.getTableName()));
        order.setConsignee(nullToEmpty(session.getTableName()));
        order.setDeliveryStatus(1);
        order.setPackAmount(0);
        order.setTablewareNumber(session.getPartySize());
        order.setTablewareStatus(1);
        ordersMapper.insert(order);

        for (DiningCartItem item : items) {
            orderDetailMapper.insert(toOrderDetail(item, order.getId()));
        }
        diningCartItemMapper.deleteBySessionId(sessionId);
        return new DiningOrderSubmitVO(order.getId(), order.getNumber(), order.getAmount(), order.getOrderTime());
    }

    @Override
    public List<?> orderedItems(Long sessionId) {
        requireActiveMember(sessionId);
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getDiningSessionId, sessionId)
                .orderByDesc(Orders::getOrderTime);
        List<Orders> orders = ordersMapper.selectList(orderWrapper);
        if (CollectionUtils.isEmpty(orders)) {
            return List.of();
        }
        List<Long> orderIds = orders.stream().map(Orders::getId).toList();
        return orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .in(OrderDetail::getOrderId, orderIds));
    }

    @Override
    public Object page(Map<String, Object> params) {
        Page<DiningSession> page = new Page<>(longParam(params, "page", 1L), longParam(params, "pageSize", 10L));
        LambdaQueryWrapper<DiningSession> wrapper = new LambdaQueryWrapper<>();
        Object status = params == null ? null : params.get("status");
        if (status != null && !String.valueOf(status).isBlank()) {
            wrapper.eq(DiningSession::getStatus, String.valueOf(status));
        }
        wrapper.orderByDesc(DiningSession::getOpenedAt);
        return diningSessionMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceClose(Long sessionId) {
        DiningSession session = requiredSession(sessionId);
        if (!DiningSession.CLOSED.equals(session.getStatus())) {
            session.setStatus(DiningSession.CLOSED);
            session.setClosedAt(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            diningSessionMapper.updateById(session);
        }
        DiningTable table = diningTableMapper.selectById(session.getTableId());
        if (table != null && sessionId.equals(table.getCurrentSessionId())) {
            table.setStatus(DiningTable.AVAILABLE);
            table.setCurrentSessionId(null);
            table.setUpdateTime(LocalDateTime.now());
            diningTableMapper.updateById(table);
        }
        diningCartItemMapper.deleteBySessionId(sessionId);
    }

    private void fillCatalogSnapshot(DiningCartItem item) {
        if (item.getDishId() != null) {
            Dish dish = dishMapper.selectById(item.getDishId());
            if (dish == null || !Integer.valueOf(1).equals(dish.getStatus())) {
                throw new BusinessException("Dish not available");
            }
            item.setName(dish.getName());
            item.setImage(dish.getImage());
            item.setAmount(dish.getPrice());
            return;
        }
        if (item.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.selectById(item.getSetmealId());
            if (setmeal == null || !Integer.valueOf(1).equals(setmeal.getStatus())) {
                throw new BusinessException("Setmeal not available");
            }
            item.setName(setmeal.getName());
            item.setImage(setmeal.getImage());
            item.setAmount(setmeal.getPrice());
            return;
        }
        throw new BusinessException("Dish or setmeal id is required");
    }

    private void deductStock(DiningCartItem item) {
        if (item.getDishId() != null) {
            deductDishStock(item.getDishId(), item.getNumber());
            return;
        }
        List<SetmealDish> dishes = setmealDishMapper.selectBySetmealId(item.getSetmealId());
        if (CollectionUtils.isEmpty(dishes)) {
            throw new BusinessException("Setmeal dishes are empty");
        }
        for (SetmealDish dish : dishes) {
            deductDishStock(dish.getDishId(), dish.getCopies() * item.getNumber());
        }
    }

    private void deductDishStock(Long dishId, Integer number) {
        if (dishMapper.deductStock(dishId, number) == 0) {
            throw new BusinessException("Dish stock is insufficient");
        }
    }

    private OrderDetail toOrderDetail(DiningCartItem item, Long orderId) {
        OrderDetail detail = new OrderDetail();
        detail.setName(item.getName());
        detail.setImage(item.getImage());
        detail.setOrderId(orderId);
        detail.setCreatorUserId(item.getCreatorUserId());
        detail.setDiningCartItemId(item.getId());
        detail.setDishId(item.getDishId());
        detail.setSetmealId(item.getSetmealId());
        detail.setDishFlavor(item.getDishFlavor());
        detail.setNumber(item.getNumber());
        detail.setAmount(item.getAmount());
        return detail;
    }

    private DiningSession requiredSession(Long sessionId) {
        DiningSession session = diningSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("Dining session not found");
        }
        return session;
    }

    private DiningSession requiredActiveSession(Long sessionId) {
        DiningSession session = requiredSession(sessionId);
        if (!DiningSession.ACTIVE.equals(session.getStatus())) {
            throw new BusinessException("Dining session is closed");
        }
        return session;
    }

    private Long requireActiveMember(Long sessionId) {
        Long userId = requiredCurrentUserId();
        requiredActiveSession(sessionId);
        if (diningSessionMemberMapper.countActiveMember(sessionId, userId) == 0) {
            throw new BusinessException("Dining session member is required");
        }
        return userId;
    }

    private DiningCartItem requiredCartItem(Long sessionId, Long itemId) {
        DiningCartItem item = diningCartItemMapper.selectById(itemId);
        if (item == null || !sessionId.equals(item.getSessionId())) {
            throw new BusinessException("Dining cart item not found");
        }
        return item;
    }

    private void requireCartItemCreator(DiningCartItem item, Long userId) {
        if (!userId.equals(item.getCreatorUserId())) {
            throw new BusinessException("Only creator can change dining cart item");
        }
    }

    private int defaultNumber(Integer number) {
        if (number == null || number < 1) {
            return 1;
        }
        return number;
    }

    private int defaultPartySize(Integer partySize) {
        if (partySize == null || partySize < 1) {
            return 1;
        }
        return partySize;
    }

    private long longParam(Map<String, Object> params, String name, long defaultValue) {
        if (params == null || params.get(name) == null || String.valueOf(params.get(name)).isBlank()) {
            return defaultValue;
        }
        return Long.parseLong(String.valueOf(params.get(name)));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String generateOrderNumber(Long userId) {
        return "DI" + System.currentTimeMillis() + String.format("%06d", userId);
    }

    private Long requiredCurrentUserId() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new BusinessException("Current user is required");
        }
        return currentId;
    }
}
