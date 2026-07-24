package com.programmers.kdt.payment.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.payment.entity.Point;
import com.programmers.kdt.payment.entity.PointLog;
import com.programmers.kdt.payment.exception.PointErrorCode;
import com.programmers.kdt.payment.repository.PointLogRepository;
import com.programmers.kdt.payment.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;
    private final PointLogRepository pointLogRepository;


    @Override
    public void usePoint(Long userId, Long amount, String eventId) {
        if (pointLogRepository.findByEventId(eventId).isPresent()) {
            log.warn("이미 처리된 포인트 사용 이벤트 - eventId:{}", eventId);
            return;
        }

        Point point = pointRepository.findById(userId)
                .orElseGet(() -> (Point.create(userId))); // 포인트 사용에서 포인트 생성이 과연 맞는가?

        try {
            point.use(amount);
            pointRepository.saveAndFlush(point); // 동시 차감으로 인한 잔액 불일치 방지
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(PointErrorCode.POINT_CONCURRENT_MODIFICATION);
        }

        pointLogRepository.save(PointLog.use(userId, amount, eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public Long findUsedAmount(String eventId) {
        return pointLogRepository.findByEventId(eventId)
                .map(PointLog::getAmount)
                .orElse(0L);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackPoint(String originEventId, Long amount, String rollbackEventId, boolean isFullRollback) {
        if (amount == null || amount <= 0) {
            return;
        }

        if (pointLogRepository.findByEventId(rollbackEventId).isPresent()) {
            log.warn("이미 처리된 포인트 환급 이벤트 - eventId:{}", rollbackEventId);
            return; // 중복 호출 방지
        }

        PointLog originLog = pointLogRepository.findByEventId(originEventId)
                .orElseThrow(() -> new BusinessException(PointErrorCode.ORIGIN_POINT_LOG_NOT_FOUND, originEventId));

        Point point = pointRepository.findById(originLog.getUserId())
                .orElseThrow(() -> new BusinessException(PointErrorCode.POINT_NOT_FOUND, originLog.getUserId()));

        try {
            point.rollbackUse(amount);
            pointRepository.saveAndFlush(point);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(PointErrorCode.POINT_CONCURRENT_MODIFICATION);
        }

        pointLogRepository.save(PointLog.rollback(originLog, amount, rollbackEventId, isFullRollback));
    }
}
