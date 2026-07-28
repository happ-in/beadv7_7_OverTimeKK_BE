package com.programmers.kdt.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReleaseTicketHoldRequest(
        @NotNull(message = "티켓 정보를 입력해주세요.")
        Long ticketId,
        @NotBlank(message = "점유 해제 키를 입력해주세요.")
        String holdKey
) {
}
