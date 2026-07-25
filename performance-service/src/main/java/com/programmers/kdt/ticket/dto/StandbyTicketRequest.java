package com.programmers.kdt.ticket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record StandbyTicketRequest(
        @NotNull(message = "티켓 정보를 입력해주세요.")
        Long ticketId,

        @NotNull(message = "대기자 정보를 입력해주세요.")
        Long standbyUserId,

        @NotNull(message = "대기만료시간을 입력해주세요.")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime standbyExpiredAt
) {
}
