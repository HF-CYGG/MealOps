package com.cjc.mealops.vo;

import java.math.BigDecimal;

public record DashboardOverviewVO(
        BigDecimal todayTurnover,
        Long todayValidOrders,
        Long newUsers,
        Long pendingOrders
) {
}
