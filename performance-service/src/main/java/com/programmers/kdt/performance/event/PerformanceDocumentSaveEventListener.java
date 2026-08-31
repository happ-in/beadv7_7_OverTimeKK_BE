package com.programmers.kdt.performance.event;

import com.programmers.kdt.search.PerformanceDocument;
import com.programmers.kdt.search.PerformanceSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PerformanceDocumentSaveEventListener {

    private final PerformanceSearchRepository performanceSearchRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void handleDocumentSave(PerformanceDocumentSaveEvent event) {
        performanceSearchRepository.save(PerformanceDocument.from(event.performance()));
    }
}
