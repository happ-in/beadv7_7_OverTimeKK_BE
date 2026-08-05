package com.programmers.kdt.settlement.client;

import com.programmers.kdt.settlement.dto.OrderResponse;

import java.util.List;

public interface OrderClient {
    List<OrderResponse> getOrders(List<Long> ticketIds);
}
