# Repository Review Instructions

## Language

- 모든 리뷰는 한국어로 작성한다.
- 코드 스타일보다 버그, 데이터 정합성, 성능, 보안을 우선적으로 검토한다.
- 문제를 지적할 때는 원인과 영향도를 함께 설명한다.
- 가능하면 수정 예시를 함께 제안한다.
- 확실하지 않은 내용은 추측하지 말고 가능성으로 표현한다.

---

# Project Architecture

프로젝트는 Spring Boot 기반 MVC 아키텍처를 사용한다.

Controller
→ Service
→ Repository

리뷰 시 다음 사항을 확인한다.

- Controller는 요청/응답 처리만 담당하는지 확인한다.
- Controller가 Repository를 직접 호출하지 않는지 확인한다.
- Service에 비즈니스 로직이 위치하는지 확인한다.
- Repository는 데이터 접근만 담당하는지 확인한다.
- 계층 간 책임이 명확하게 분리되어 있는지 확인한다.

---

# Transaction

다음 사항을 우선적으로 검토한다.

- 상태 변경 메서드에 @Transactional이 필요한지 확인한다.
- Transaction 범위가 과도하게 넓지 않은지 확인한다.
- Event 발행 시점이 적절한지 확인한다.
- AFTER_COMMIT 사용이 적절한지 검토한다.
- REQUIRES_NEW 사용이 실제 필요한지 검토한다.

---

# Event

프로젝트는 Spring Event 기반 비동기 처리를 사용한다.

다음을 확인한다.

- Event 이름이 명확한지
- Event Payload가 최소한의 데이터만 포함하는지
- Event Listener가 중복 조회를 수행하지 않는지
- 동일 Event가 여러 번 수신되어도 멱등성이 보장되는지
- Event 실패 시 데이터 정합성이 깨질 가능성이 없는지

---

# Async

비동기 코드에서는 다음을 확인한다.

- @Async 사용이 적절한지
- Exception이 유실되지 않는지
- Thread Safe 하지 않은 객체를 공유하지 않는지
- 재시도 또는 보상 처리가 필요한지

---

# Performance Domain

공연 관련 코드에서는 다음을 검토한다.

- 공연 시작 이후 수정 또는 삭제가 가능한지
- Ticket Open 이후 수정이 가능한지
- Session 생성 시 중복 데이터가 발생하지 않는지
- Ticket 생성이 대량 데이터에 적합한 방식인지

---

# Ticket Domain

Ticket 상태는 다음 전이만 허용한다.

AVAILABLE
→ HOLD

또는

HOLD
→ AVAILABLE
→ CANCELED
→ RESERVED

또는

RESERVED
→ AVAILABLE
→ CANCELED

다음을 중점적으로 확인한다.

- 동일 좌석이 동시에 HOLD 가능한지
- 동일 좌석이 중복 RESERVED 가능한지
- HOLD 만료와 결제 완료가 동시에 발생하는 경우를 고려했는지
- 결제 실패 시 HOLD가 정상 해제되는지
- 상태 변경이 원자적으로 수행되는지

---

---

# Order Domain

주문 관련 코드는 다음 사항을 우선적으로 검토한다.

- 하나의 주문이 중복 생성되지 않는지 확인한다.
- 주문 생성 시 Ticket 상태가 올바른지 확인한다.
- 이미 HOLD 또는 RESERVED 상태인 Ticket으로 주문이 생성되지 않는지 확인한다.
- 주문 생성과 Ticket 상태 변경이 하나의 비즈니스 흐름으로 처리되는지 확인한다.
- 주문 취소 시 Ticket 상태가 정상적으로 복구되는지 확인한다.
- 동일 사용자가 동일 좌석에 대해 중복 주문할 수 없는지 확인한다.
- 예외 발생 시 데이터가 중간 상태로 남지 않는지 확인한다.

---

# Payment Domain

결제 관련 코드는 다음 사항을 우선적으로 검토한다.

- 결제 성공 시 Ticket 상태가 반드시 RESERVED로 변경되는지 확인한다.
- 결제 실패 또는 취소 시 Ticket 상태가 반드시 AVAILABLE 또는 CANCELED로 복구되는지 확인한다.
- 결제 상태 변경이 Transaction 안에서 안전하게 처리되는지 확인한다.
- PG 응답이 지연되거나 중복 전달되는 상황을 고려했는지 확인한다.
- 결제 완료 후 후속 Event가 실패해도 데이터 정합성이 유지되는지 확인한다.
- 결제 완료 이후 동일 주문에 대한 추가 결제가 발생하지 않는지 확인한다.

---

# Validation

비즈니스 유효성 검증을 우선적으로 확인한다.

다음을 검토한다.

- 요청 데이터가 충분히 검증되는지
- 존재하지 않는 Ticket을 사용할 수 없는지
- 존재하지 않는 Order를 사용할 수 없는지
- 존재하지 않는 Payment를 사용할 수 없는지
- 공연 시작 이후 예매가 가능한지
- Ticket Open 이전 예매가 가능한지
- Ticket 상태가 현재 비즈니스 규칙에 맞는지
- 동일 사용자의 중복 요청이 허용되는지

입력값 검증보다 비즈니스 규칙 검증을 우선적으로 리뷰한다.

---

# Idempotency

다음 기능은 반드시 멱등성을 보장해야 한다.

- 주문 생성
- 주문 취소
- 결제 요청
- 결제 성공 처리
- 결제 실패 처리
- Ticket 상태 변경
- Event Listener

다음을 중점적으로 검토한다.

- 동일 요청이 여러 번 호출되어도 동일한 결과를 유지하는지
- 중복 Event를 안전하게 처리하는지
- 이미 처리된 요청을 다시 처리하지 않는지
- 상태 전이가 중복 수행되지 않는지
- 외부 PG Callback이 여러 번 전달되어도 문제가 없는지

---

# Concurrency

다음을 적극적으로 검토한다.

- Race Condition
- Lost Update
- 동일 Ticket 동시 접근
- Lock 적용이 필요한지
- Redis 또는 Database Lock 사용이 적절한지

동시성 문제가 발생할 가능성이 있다면 반드시 리뷰한다.

---

# JPA

다음을 확인한다.

- N+1 Query
- Fetch Join 필요 여부
- Lazy Loading 문제
- save() 반복 호출
- saveAll() 사용 적절성
- Batch Insert 가능 여부
- Entity를 Response로 직접 반환하는지
- Optional 사용이 적절한지
- Long 비교 시 == 사용 여부

---

# API

다음을 확인한다.

- RESTful URL인지
- HTTP Method가 적절한지
- Request/Response DTO를 사용하는지
- Validation이 충분한지
- Error Code가 일관적인지

---

# Exception

다음을 확인한다.

- 적절한 Custom Exception을 사용하는지
- Global Exception Handler에서 처리 가능한지
- 예외 로그가 충분한지

---

# Logging

다음을 확인한다.

- Error Log가 필요한 위치인지
- 민감정보를 출력하지 않는지
- 로그 레벨이 적절한지

---

# Test

다음 테스트를 우선 제안한다.

- 정상 케이스
- 실패 케이스
- 경계값 테스트
- 동시성 테스트
- Event Listener 테스트

---

# Security

다음을 확인한다.

- 인증 및 권한 검증이 적절한지
- 입력값 검증이 충분한지
- 민감정보가 노출되지 않는지

---

# Review Priority

다음 순서로 리뷰한다.

1. 데이터 정합성
2. 동시성
3. Transaction
4. Event 처리
5. 보안
6. 성능
7. 유지보수성
8. 코드 스타일

스타일보다 실제 장애 가능성이 있는 문제를 우선 리뷰한다.

## Critical Review Focus

다음 항목은 일반 코드 품질보다 우선적으로 검토한다.

- 중복 결제
- 중복 주문
- 중복 예매
- 데이터 정합성
- 동시성 문제
- 멱등성 보장
- Transaction 경계
- Event 처리 순서
- Ticket 상태 전이의 무결성

이와 관련된 잠재적인 버그나 장애 가능성이 있다면 반드시 리뷰하고 개선 방안을 제안한다.

# Review Priority

다음 순서로 리뷰한다.

1. 데이터 정합성(Consistency)
2. 동시성(Concurrency)
3. 멱등성(Idempotency)
4. 주문/결제 비즈니스 로직
5. Transaction
6. Event 처리
7. 보안(Security)
8. 성능(Performance)
9. 유지보수성
10. 코드 스타일

코드 스타일보다 실제 서비스 장애, 중복 결제, 중복 주문, 데이터 불일치 가능성을 우선적으로 리뷰한다.

# Consistency

데이터 정합성을 가장 중요한 리뷰 항목으로 간주한다.

다음을 확인한다.

- Order와 Payment 상태가 서로 일치하는지
- Payment 성공 시 Ticket 상태가 반드시 RESERVED인지
- Payment 실패 시 Ticket 상태가 반드시 AVAILABLE or CANCELED인지
- Ticket 상태와 Order 상태가 서로 모순되지 않는지
- Transaction 종료 이후에도 데이터가 일관된 상태를 유지하는지
- Event 처리 이후에도 데이터 정합성이 유지되는지
- 예외 발생 시 Rollback 또는 보상 처리가 적절한지
- 여러 테이블이 함께 변경되는 경우 원자성이 보장되는지

데이터 정합성 문제는 가장 높은 우선순위로 리뷰한다.