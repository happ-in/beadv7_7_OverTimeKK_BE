package com.programmers.kdt.payment.service;

public interface PointService {

    // 포인트 사용 (결제 시점에 호출)
    void usePoint(Long userId, Long amount, String eventId);

    // 포인트 적립 (공연 종료 배치에서 호출)
    void earnPoint(Long userId, Long amount, String eventId);

    // eventId로 남겨진 포인트 사용/환급 금액을 조회
    Long findUsedAmount(String eventId);

    // 포인트 환급(결제 실패/환불 완료 시점에 호출)
    void rollbackPoint(String originEventId, Long amount, String rollbackEventId, boolean isFullRollback);
}
