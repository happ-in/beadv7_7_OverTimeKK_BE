# kdt — MSA 멀티모듈 스켈레톤

팀 결정 사항 반영:
- 진짜 MSA (서버/DB 실제 분리), 단 레포는 모노레포 + 멀티모듈 (EC2 1대 제약 고려)
- DB는 같은 MySQL 서버, 스키마만 분리 (`user_db`, `order_db`, `performance_db`)
- 서비스 간 통신은 메시지 브로커 대신 REST 호출 (EC2 리소스 절약)
- 내부 구조는 Controller-Service-Repository-Entity 단순 MVC (인터페이스 분리 없음)
- `Member` → `User`로 클래스명/패키지명 변경

## 전체 구조

```
kdt/
├── settings.gradle              # 3개 모듈 등록
├── build.gradle                  # 공통 의존성 (subprojects)
├── init-databases.sql            # DB 3개 생성 스크립트
├── user-service/                 # 포트 8081
│   └── src/main/java/com/programmers/kdt/user/
│       ├── controller/ service/ repository/ entity/ dto/
├── order-service/                # 포트 8082
│   └── src/main/java/com/programmers/kdt/order/
│       ├── controller/ service/ repository/ entity/ dto/
│       └── service/UserClient.java   # user-service REST 호출 예시
└── performance-service/          # 포트 8083
    └── src/main/java/com/programmers/kdt/performance/
        ├── controller/ service/ repository/ entity/ dto/
```

## 1. 프로젝트 통째로 교체

기존 `beadv7_7_OverTimeKK_BE` 안의 단일 모듈 구조(`src/main/java/com/programmers/kdt/...`)를
이 멀티모듈 구조로 **통째로 교체**해야 합니다. 지금까지 만든 단일 모듈 스켈레톤과는
디렉토리 레이아웃 자체가 다릅니다 (루트에 `settings.gradle`이 여러 모듈을 포함하는 구조).

1. 기존 `src/` 폴더, 기존 `build.gradle`은 삭제 (또는 백업)
2. 이 zip의 `settings.gradle`, `build.gradle`, `init-databases.sql`,
   `user-service/`, `order-service/`, `performance-service/` 를
   레포 루트에 그대로 복사

## 2. IntelliJ에서 열기

기존 프로젝트를 닫고, 레포 루트 폴더를 `File → Open`으로 다시 엽니다.
IntelliJ가 `settings.gradle`을 보고 **3개의 모듈이 있는 멀티모듈 프로젝트**로 인식합니다.
(우측 Gradle 탭에 `kdt` 밑에 `user-service`, `order-service`, `performance-service`가
각각 하위 프로젝트로 보이면 정상)

## 3. DB 준비

로컬 MySQL에 접속해서 `init-databases.sql` 실행 (스키마 3개 생성).

## 4. 각 서비스 실행 (로컬 개발 시 터미널 3개 필요)

```powershell
./gradlew :user-service:bootRun
./gradlew :order-service:bootRun
./gradlew :performance-service:bootRun
```

터미널 3개를 따로 열어서 각각 실행하거나, IntelliJ에서 각 모듈의
`XxxServiceApplication` 클래스를 우클릭 → Run 으로 3개를 동시에 띄우면 됩니다.
정상 실행되면:
- `http://localhost:8081` — user-service
- `http://localhost:8082` — order-service
- `http://localhost:8083` — performance-service

## 5. 서비스 간 통신 확인 포인트

`order-service`의 `UserClient.java`가 `user-service.url`(application.properties에 정의)로
REST 호출을 하는 자리입니다. 지금은 `existsUser()`가 TODO 상태이니,
`OrderService.createOrder()`를 구현할 때 이 클라이언트를 통해
"주문하려는 회원이 실제로 존재하는지" 확인하는 로직을 채우면 됩니다.

## 6. 담당자별 확인 사항

| 담당 | 모듈 | 확인할 것 |
|---|---|---|
| 회원 (1명) | `user-service` | `UserController.getUser()` — 다른 서비스가 호출할 내부용 API도 채워야 함 |
| 결제/정산 (2명) | `order-service` | `UserClient.existsUser()` 실제 구현, `SettlementPolicy` 컬럼 확정 |
| 공연/티켓 (2명) | `performance-service` | `Standby.zone1/2/3` 구조 재확인, `PerformanceService.cancelPerformance()`에서 order-service 호출할 `OrderClient` 추가 필요 |

## 7. 최신 ERD 반영 변경 이력 (이번 업데이트)

- `Payment`에 `paymentStatus`(`PaymentStatus` enum: SUCCESS/FAILED/CANCELLED) 필드 추가
- `Performance`에 `ticketOpenAt` 필드 추가 — ERD엔 LocalDate로만 보였는데, "티켓오픈시간"이라는 이름상 분·초까지 필요할 것 같아 **LocalDateTime으로 지정해뒀습니다. 팀 확인 필요**
- 신규 테이블 `PerformanceSeatPrice` 추가 — 같은 홀을 여러 공연이 같이 쓸 때, 공연마다 좌석 가격을 다르게 매길 수 있도록 만든 테이블 (`perfomanceInfoId` + `hallId` + `zone` 조합별 가격)
- **아직 반영 안 한 것**: ERD에 있던 `File_path`(Untitled2) 테이블 — 필드가 전부 `Field`, `Field2`... 같은 placeholder라 실제로 뭘 저장하는 테이블인지 확정이 안 돼서 스켈레톤에 안 넣었습니다. 팀에서 용도(예: 이미지 첨부 경로?)와 필드를 정하면 알려주세요, 바로 추가해드릴게요

## 8. 나중에 EC2 배포 시 (참고, 지금 당장은 안 해도 됨)

3개 서비스 + MySQL을 EC2 하나에 올리려면 각 서비스를 jar로 빌드해서
`java -jar` 로 포트를 다르게(8081/8082/8083) 실행하거나, Docker Compose로
컨테이너 4개(user-service, order-service, performance-service, mysql)를
한 번에 띄우는 방식을 추천합니다. 이 부분은 실제 기능 구현이 어느 정도
끝난 뒤에 다시 다뤄도 늦지 않습니다.
