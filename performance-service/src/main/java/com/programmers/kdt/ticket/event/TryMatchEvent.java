package com.programmers.kdt.ticket.event;

import jakarta.validation.constraints.NotNull;

public record TryMatchEvent(
        @NotNull(message = "티켓 정보를 입력해주세요.")
        Long ticketId
) {
}
