package com.programmers.kdt.ticket.event;

import com.programmers.kdt.ticket.entity.TicketStatus;

public record TicketIssueEvent(Long performanceId, TicketStatus ticketStatus) {
}