package com.programmers.kdt.performance.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.performance.dto.RegisterPerformanceRequest;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceSeatPrice;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.repository.PerformanceRepository;
import com.programmers.kdt.performance.repository.PerformanceSeatPriceRepository;
import com.programmers.kdt.performance.repository.PerformanceSessionRepository;
import com.programmers.kdt.ticket.entity.Ticket;
import com.programmers.kdt.ticket.repository.TicketRepository;
import com.programmers.kdt.venue.entity.Seat;
import com.programmers.kdt.venue.exception.VenueException;
import com.programmers.kdt.venue.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceV2Service {

    private final PerformanceRepository performanceRepository;
    private final PerformanceSessionRepository sessionRepository;
    private final PerformanceSeatPriceRepository seatPriceRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public void registerPerformanceInformation(RegisterPerformanceRequest request) {
        // 1. 공연등록
        Performance performance = performanceRepository.save(request.toPerformance());

        // 2. 회차등록
        List<PerformanceSession> sessions = request.toSessions(performance);
        sessionRepository.saveAll(sessions);

        // 3. 좌석 등급별 금액 등록
        List<PerformanceSeatPrice> seatPrices = request.toSeatPrices(performance);
        seatPriceRepository.saveAll(seatPrices);

        // 4. Ticket
        List<Ticket> tickets = new ArrayList<>();
        for (PerformanceSeatPrice seatPrice : seatPrices) {
            List<Seat> seats = seatRepository.findByHall_HallIdAndZone(performance.getHallId(), seatPrice.getZone());

            for (PerformanceSession session : sessions) {
                if (seats.isEmpty()) {
                    throw new BusinessException(VenueException.SEAT_INFORMATION_NOT_FOUND, performance.getHallId());
                }

                for (Seat seat : seats) {
                    Ticket ticket = Ticket.create(
                            performance.getPerformanceId(),
                            session.getPerformanceSessionId().getSessionNum(),
                            seat.getZone(),
                            seat.getSeatRow(),
                            seat.getSeatNum(),
                            seatPrice.getPrice()
                            );
                    tickets.add(ticket);
                }
            }
        }
        ticketRepository.saveAll(tickets);
    }
}
