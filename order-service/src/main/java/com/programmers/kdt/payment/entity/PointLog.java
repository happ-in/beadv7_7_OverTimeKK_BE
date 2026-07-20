package com.programmers.kdt.payment.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false)
    private PointType pointType;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "ref_log_id")
    private Long refLogId; // 취소 로그일 때만 채워짐, 원본 USE 로그를 참조(어떤 사용건에 대한 취소?)

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId; // 이벤트 중복 처리 방지(두번 지급, 두번 사용등), UUID 사용

    // 적립 내역 생성 메서드
    public static PointLog earn(Long userId, Long amount, String eventId) {
        validateCommon(userId, amount, eventId);
        PointLog pointLog = new PointLog();
        pointLog.userId = userId;
        pointLog.pointType = PointType.EARN;
        pointLog.amount = amount;
        pointLog.eventId = eventId;
        return pointLog;
    }

    // 사용 내역 생성 메서드
    public static PointLog use(Long userId, Long amount, String eventId) {
        validateCommon(userId, amount, eventId);
        PointLog pointLog = new PointLog();
        pointLog.userId = userId;
        pointLog.pointType = PointType.USE;
        pointLog.amount = amount;
        pointLog.eventId = eventId;
        return pointLog;
    }

    // 환급 내역 생성 메서드
    public static PointLog rollback(PointLog originPointLog, Long amount, String eventId, boolean isFullRollback) {
        if (originPointLog == null || originPointLog.getPointType() != PointType.USE) {
            throw new IllegalArgumentException("환급은 USE 로그만 가능합니다.");
        }

        validateCommon(originPointLog.getUserId(), amount, eventId);

        if (amount > originPointLog.getAmount()) {
            throw new IllegalArgumentException("취소 금액이 원본 사용금액을 초과했습니다. 원본 : " + originPointLog.getAmount() +
                    "취소 금액 :" + amount);
        }

        PointLog pointLog = new PointLog();
        pointLog.userId = originPointLog.getUserId();
        pointLog.pointType = isFullRollback ? PointType.CANCELLED : PointType.PARTIAL_CANCELLED;
        pointLog.amount = amount;
        pointLog.refLogId = originPointLog.getId();
        pointLog.eventId = eventId;
        return pointLog;
    }

    // 공통 예외처리 메서드
    private static void validateCommon(Long userId, Long amount, String eventId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId가 없습니다.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("금액은 0원보다 커야 합니다. 금액 : " + amount);
        }
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId가 없습니다.");
        }
    }
}
