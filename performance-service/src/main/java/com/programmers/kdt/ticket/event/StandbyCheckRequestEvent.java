package com.programmers.kdt.ticket.event;

public record StandbyCheckRequestEvent(
        Long performanceId,
        Long sessionNum,
        String zone,
        Long ticketId
) {
}
