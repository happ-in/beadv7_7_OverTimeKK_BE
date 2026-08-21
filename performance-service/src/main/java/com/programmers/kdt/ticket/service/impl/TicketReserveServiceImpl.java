package com.programmers.kdt.ticket.service.impl;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.ticket.dto.ReservedTicketRequest;
import com.programmers.kdt.ticket.entity.Ticket;
import com.programmers.kdt.ticket.entity.TicketStatus;
import com.programmers.kdt.ticket.event.StandbyTicketReservedEvent;
import com.programmers.kdt.ticket.exception.TicketErrorCode;
import com.programmers.kdt.ticket.repository.TicketRepository;
import com.programmers.kdt.ticket.service.TicketReserveService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketReserveServiceImpl implements TicketReserveService {

    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void reservedTicket(ReservedTicketRequest request) {
        Ticket ticket = getTicket(request.ticketId());
        validatePossibleReservedHoldTicket(ticket, request.holdKey());
        ticket.reservedTicket(request.userId());
        //대기표에서 매칭된 상태의 경우
        if (ticket.getStandbyUserId() != null) {
            eventPublisher.publishEvent(new StandbyTicketReservedEvent(ticket.getTicketId()));
        }
    }

    private static void validatePossibleReservedHoldTicket(Ticket ticket, String holdKey) {
        validateNotReservedTicket(ticket);
        ticket.validateHoldStatus();

        if (LocalDateTime.now().isAfter(ticket.getHoldExpiredAt())) {
            throw new BusinessException(TicketErrorCode.HOLD_TICKET_EXPIRED);
        }

        if (holdKey == null || !holdKey.equals(ticket.getHoldKey())) {
            throw new BusinessException(TicketErrorCode.HOLD_KEY_DISCREPANCY);
        }
    }

    private static void validateNotReservedTicket(Ticket ticket) {
        if (ticket.getTicketStatus() == TicketStatus.RESERVED) {
            throw new BusinessException(TicketErrorCode.ALREADY_RESERVED_TICKET);
        }
    }

    private @NonNull Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(TicketErrorCode.TICKET_NOT_FOUND));
    }
}