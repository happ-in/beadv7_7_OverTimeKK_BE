package com.programmers.kdt.order.client;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.order.dto.TicketHoldRequest;
import com.programmers.kdt.order.dto.TicketHoldResult;
import com.programmers.kdt.order.dto.TicketReleaseRequest;
import com.programmers.kdt.order.dto.TicketCancelRequest;
import com.programmers.kdt.order.dto.TicketReserveRequest;
import com.programmers.kdt.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TicketApiClient implements TicketClient {

    private final RestClient restClient;

    @Override
    public TicketHoldResult holdSeat(TicketHoldRequest ticketRequest){
        try{
            ApiResponse<TicketHoldResult> response = restClient.put()
                    .uri("/api/tickets/status/hold")
                    .body(ticketRequest)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<TicketHoldResult>>() {});
            if(response == null || response.data() == null){
                throw new BusinessException(OrderErrorCode.TICKET_HOLD_RESPONSE_EMPTY);
            }
            return response.data();

        } catch (RestClientException e){
            throw new BusinessException(OrderErrorCode.TICKET_HOLD_FAILED);
        }
    }

    @Override
    public void reserveTicket(TicketReserveRequest request){
        try {
            restClient.put()
                    .uri("/api/tickets/status/reserved")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException e) {
            throw new BusinessException(OrderErrorCode.TICKET_RESERVE_FAILED);
        }
    }
    @Override
    public void releaseSeat(TicketReleaseRequest request) {
        try {
            restClient.put()
                    .uri("/api/tickets/status/release")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException e) {
            throw new BusinessException(OrderErrorCode.TICKET_RELEASE_FAILED);
        }
    }

    @Override
    public void cancelTicket(TicketCancelRequest request) {
        try {
            restClient.put()
                    .uri("/api/tickets/status/canceled/release")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException e) {
            throw new BusinessException(OrderErrorCode.TICKET_RELEASE_FAILED);
        }
    }

    @Override
    public List<TicketInfo> getTickets(Long userId) {
        try {
            ApiResponse<List<TicketInfo>> response = restClient.get()
                    .uri("/api/tickets/orders?userId={userId}", userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<
                            ApiResponse<List<TicketInfo>>
                            >() {});

            if (response == null || response.data() == null) {
                throw new BusinessException(OrderErrorCode.TICKET_INFO_RESPONSE_EMPTY);
            }

            return response.data();

        } catch (RestClientException e) {
            throw new BusinessException(OrderErrorCode.TICKET_INFO_GET_FAILED);
        }
    }
}
