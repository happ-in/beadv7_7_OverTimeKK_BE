package com.programmers.kdt.order.scheduler;

import com.programmers.kdt.order.entity.TicketCancelJob;
import com.programmers.kdt.order.entity.TicketCancelJobStatus;
import com.programmers.kdt.order.repository.TicketCancelJobRepository;
import com.programmers.kdt.order.service.TicketCancelJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TicketCancelJobScheduler {

    private final TicketCancelJobRepository ticketCancelJobRepository;
    private final TicketCancelJobService ticketCancelJobService;

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void cancelTicket(){
        for(TicketCancelJob job : ticketCancelJobRepository.findAllByStatus(TicketCancelJobStatus.PENDING)){
            ticketCancelJobService.process(job.getOrderId(), job.getTicketId(), job.getUserId());
        }
    }
}
