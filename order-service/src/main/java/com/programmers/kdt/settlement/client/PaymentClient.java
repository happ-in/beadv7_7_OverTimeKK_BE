package com.programmers.kdt.settlement.client;

import com.programmers.kdt.settlement.dto.PaymentResponse;
import java.util.List;

public interface PaymentClient {
    List<PaymentResponse> getPayments(List<Long> orderIds);
}
