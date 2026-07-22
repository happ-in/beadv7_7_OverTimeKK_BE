package com.programmers.kdt.payment.client.refund;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.common.exception.CommonErrorCode;
import com.programmers.kdt.order.entity.OrderItem;
import com.programmers.kdt.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalOrderClient implements OrderClient{

    private final OrderItemRepository orderItemRepository;

    @Override
    public Long getTicketId(Long orderId) {
        return orderItemRepository.findFirstByOrderId(orderId)
                .map(OrderItem::getTicketId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.NOT_FOUND));
    }
}
