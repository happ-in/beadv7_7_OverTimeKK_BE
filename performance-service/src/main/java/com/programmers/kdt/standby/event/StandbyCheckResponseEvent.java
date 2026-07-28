package com.programmers.kdt.standby.event;

public record StandbyCheckResponseEvent(
        Long ticketId,
        boolean existsStandby
) {
}
