package com.programmers.kdt.ticket.controller;

import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.ticket.dto.CancelTicketStatusRequest;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableRequest;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableResponse;
import com.programmers.kdt.ticket.dto.CreateStandbyResponse;
import com.programmers.kdt.ticket.dto.OrderTicketRequest;
import com.programmers.kdt.ticket.dto.OrderTicketResponse;
import com.programmers.kdt.ticket.dto.ReleaseTicketHoldRequest;
import com.programmers.kdt.ticket.dto.ReservedTicketRequest;
import com.programmers.kdt.ticket.dto.SessionStartDateResponse;
import com.programmers.kdt.ticket.dto.TicketZoneRequest;
import com.programmers.kdt.ticket.dto.TicketZonesResponse;
import com.programmers.kdt.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/standby")
    public CreateStandbyResponse issueStandby(
            @RequestParam Long userId, @RequestParam Long sessionNum, @RequestParam String zone) {
        return ticketService.issueStandby(userId, sessionNum, zone);
    }

    @GetMapping("/{ticketId}/refund-date")
    public ApiResponse<SessionStartDateResponse> getSessionStartDateResponse(@PathVariable Long ticketId) {
        SessionStartDateResponse response = ticketService.getSessionStartDate(ticketId);
        return ApiResponse.success(response);
    }

    @PutMapping("/status/hold")
    public ApiResponse<CheckTicketHoldAvailableResponse> checkTicketHoldAvailable(@Valid @RequestBody CheckTicketHoldAvailableRequest request) {
        CheckTicketHoldAvailableResponse response = ticketService.checkTicketHoldStatus(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/status/release")
    public ApiResponse<?> releaseHoldTicket(@Valid @RequestBody ReleaseTicketHoldRequest request) {
        ticketService.releaseHoldTicket(request);
        return ApiResponse.success(null);
    }

    @PutMapping("/status/canceled/release")
    public ApiResponse<?> releaseCanceledTicket(@Valid @RequestBody CancelTicketStatusRequest request) {
        ticketService.cancelReservedTicket(request);
        return ApiResponse.success(null);
    }

    @PutMapping("/status/reserved")
    public ApiResponse<?> reservedTicket(@Valid @RequestBody ReservedTicketRequest request) {
        ticketService.reservedTicket(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/orders")
    public ApiResponse<List<OrderTicketResponse>> orderTickets(@Valid @RequestBody OrderTicketRequest request) {
        return ApiResponse.success(ticketService.findOrderedTicketInfo(request.userId()));
    }

    @PostMapping("/select/seat")
    public ApiResponse<TicketZonesResponse> selectZone(@Valid @RequestBody TicketZoneRequest request) {
        return ApiResponse.success(ticketService.getTicketZone(request));
    }
}
