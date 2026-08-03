package com.programmers.kdt.ticket.service;

import com.programmers.kdt.common.TimeLimits;
import com.programmers.kdt.common.constant.OrderTypeCode;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.common.exception.CommonErrorCode;
import com.programmers.kdt.common.util.TicketKeyGenerator;
import com.programmers.kdt.standby.event.StandbyCheckResponseEvent;
import com.programmers.kdt.ticket.dto.CancelTicketStatusRequest;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableRequest;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableResponse;
import com.programmers.kdt.ticket.dto.ReleaseTicketHoldRequest;
import com.programmers.kdt.ticket.dto.ReservedTicketRequest;
import com.programmers.kdt.ticket.entity.Ticket;
import com.programmers.kdt.ticket.entity.TicketStatus;
import com.programmers.kdt.ticket.event.StandbyCheckRequestEvent;
import com.programmers.kdt.ticket.exception.TicketErrorCode;
import com.programmers.kdt.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeTicketStatusServiceImpl implements ChangeTicketStatusService {

    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CheckTicketHoldAvailableResponse checkTicketHoldStatus(CheckTicketHoldAvailableRequest request) {
        Ticket ticket = getTicket(request.ticketId());
        LocalDateTime holdExpiredAt = LocalDateTime.now().plusMinutes(TimeLimits.orderHoldTicket5Min);
        String holdKey = TicketKeyGenerator.generate();

        validateTicketConditionForHold(request.orderType(), ticket, request.userId());

        ticket.holdTicket(request.userId(), holdExpiredAt, holdKey);
        return new CheckTicketHoldAvailableResponse(ticket.getTicketId(), ticket.getPrice(), holdExpiredAt, holdKey);
    }

    @Override
    @Transactional
    public void releaseHoldTicket(ReleaseTicketHoldRequest request) {
        Ticket ticket = getTicket(request.ticketId());
        validatePossibleReleasedHoldTicket(ticket, request.holdKey());
        releaseTicket(ticket);
    }

    @Override
    public List<Long> findExpiredHoldTicketIds() {
        return ticketRepository.findByTicketStatusAndHoldExpiredAtBefore(TicketStatus.HOLD, LocalDateTime.now())
                .stream()
                .map(Ticket::getTicketId)
                .toList();
    }

    @Override
    @Transactional
    public void releaseExpiredHoldTicket(Long ticketId, Map<SessionZoneKey, Boolean> zoneAvailabilityCache) {
        Ticket ticket = getTicket(ticketId);
        if (ticket.getTicketStatus() != TicketStatus.HOLD) {
            return;
        }
        releaseTicket(ticket, zoneAvailabilityCache);
    }

    @Override
    @Transactional
    public void changeTicketStatusByStandby(StandbyCheckResponseEvent event) {
        Ticket ticket = getTicket(event.ticketId());

        if (event.existsStandby()) {
            ticket.releaseToStandby();
        } else {
            ticket.releaseToAvailable();
        }
    }

    @Override
    @Transactional
    public void cancelReservedTicket(CancelTicketStatusRequest request) {
        Ticket ticket = getTicket(request.ticketId());
        validateReservedTicket(ticket, request.userId());
        releaseTicket(ticket);
    }

    @Override
    @Transactional
    public void reservedTicket(ReservedTicketRequest request) {
        Ticket ticket = getTicket(request.ticketId());
        validatePossibleReservedHoldTicket(ticket, request.holdKey());
        ticket.reservedTicket(request.userId());
    }

    private static void validateNotReservedTicket(Ticket ticket) {
        if (ticket.getTicketStatus() == TicketStatus.RESERVED) {
            throw new BusinessException(TicketErrorCode.ALREADY_RESERVED_TICKET);
        }
    }

    private void validateTicketConditionForHold(String orderType, Ticket ticket, Long userId) {
        switch (orderType) {
            case OrderTypeCode.GENERAL -> validateAvailableToHoldTicket(ticket);
            case OrderTypeCode.STANDBY -> validateStandbyToHoldTicket(ticket, userId);
            default -> throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE, "주문구분값");
        }
    }

    private static void validateAvailableToHoldTicket(Ticket ticket) {
        if (ticket.getTicketStatus() != TicketStatus.AVAILABLE) {
            throw new BusinessException(TicketErrorCode.IMPOSSIBLE_HOLD_TICKET);
        }
    }

    private static void validateStandbyToHoldTicket(Ticket ticket, Long userId) {
        if (!isStandbyTicket(ticket, userId)) {
            throw new BusinessException(TicketErrorCode.STANDBY_TICKET_DISCREPANCY);
        }
    }

    private static void validatePossibleReservedHoldTicket(Ticket ticket, String holdKey) {
        validateNotReservedTicket(ticket);
        validateHoldStatus(ticket);

        if (LocalDateTime.now().isAfter(ticket.getHoldExpiredAt())) {
            throw new BusinessException(TicketErrorCode.HOLD_TICKET_EXPIRED);
        }

        if (holdKey == null || !holdKey.equals(ticket.getHoldKey())) {
            throw new BusinessException(TicketErrorCode.HOLD_KEY_DISCREPANCY);
        }
    }

    private static void validatePossibleReleasedHoldTicket(Ticket ticket, String holdKey) {
        validateHoldStatus(ticket);

        if (LocalDateTime.now().isBefore(ticket.getHoldExpiredAt())) {
            if (holdKey == null || !holdKey.equals(ticket.getHoldKey())) {
                throw new BusinessException(TicketErrorCode.HOLD_KEY_DISCREPANCY);
            }
        }
    }

    private static void validateHoldStatus(Ticket ticket) {
        if (ticket.getTicketStatus() != TicketStatus.HOLD) {
            throw new BusinessException(TicketErrorCode.TICKET_IS_NOT_HELD);
        }
    }

    private static void validateReservedTicket(Ticket ticket, Long userId) {
        if (ticket.getTicketStatus() != TicketStatus.RESERVED) {
            throw new BusinessException(TicketErrorCode.NOT_RESERVED_TICKET);
        }

        if (!userId.equals(ticket.getBuyUserId())) {
            throw new BusinessException(TicketErrorCode.TICKET_OWNER_DISCREPANCY);
        }
    }

    private void releaseTicket(Ticket ticket) {
        releaseTicket(ticket, new HashMap<>());
    }

    private void releaseTicket(Ticket ticket, Map<SessionZoneKey, Boolean> availabilityCache) {
        SessionZoneKey key = new SessionZoneKey(ticket.getPerformanceId(), ticket.getSessionNum(), ticket.getZone());
        boolean existsAvailableInZone = availabilityCache.computeIfAbsent(key, k ->
                ticketRepository.existsByPerformanceIdAndSessionNumAndTicketStatusAndZone(k.performanceId(), k.sessionNum(), TicketStatus.AVAILABLE, k.zone()));
        log.info("{} 공연 {}회차 {} 구역 매진이 아닌가? {}", ticket.getPerformanceId(), ticket.getSessionNum(), ticket.getZone(), existsAvailableInZone);

        if (existsAvailableInZone) {
            ticket.releaseToAvailable();
        } else {
            eventPublisher.publishEvent(new StandbyCheckRequestEvent(ticket.getPerformanceId(), ticket.getSessionNum(), ticket.getZone(), ticket.getTicketId()));
        }
    }

    private static boolean isStandbyTicket(Ticket ticket, Long userId) {
        return ticket.getTicketStatus() == TicketStatus.CANCELED &&
                userId.equals(ticket.getStandbyUserId()) &&
                LocalDateTime.now().isBefore(ticket.getStandbyExpiredAt())
        ;
    }

    private @NonNull Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(TicketErrorCode.TICKET_NOT_FOUND));
    }
}