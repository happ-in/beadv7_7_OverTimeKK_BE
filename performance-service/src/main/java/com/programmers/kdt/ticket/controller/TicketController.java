package com.programmers.kdt.ticket.controller;

import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.ticket.dto.CreateStandbyResponse;
import com.programmers.kdt.ticket.dto.SessionStartDateResponse;
import com.programmers.kdt.ticket.service.TicketServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketServiceImpl ticketService;

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
}
