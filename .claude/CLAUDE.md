# CLAUDE.md

이 파일은 Claude Code(claude.ai/code)가 이 저장소에서 작업할 때 참고하는 가이드입니다.

## 프로젝트 개요

Morupark는 MSA(마이크로서비스 아키텍처)로 구축된 티켓 대기열 관리 시스템입니다. 선착순 티켓팅 이벤트에서 대기열 관리, 추첨 처리, 결과 통지를 담당합니다.

**4개의 마이크로서비스:**
- `services-api-gateway` (포트 8000) — Spring Cloud Gateway; 요청 라우팅 및 JWT 검증
- `services-auth` (포트 8081) — JWT 토큰 발급
- `services-queue` (포트 8082) — Redis 기반 티켓 대기열 관리; Kafka 메시지 produce/consume
- `services-goods` (포트 8083) — 추첨 처리 및 당첨자 MySQL 저장; Kafka 메시지 produce/consume

## 빌드 및 실행 명령어

```bash
# 전체 빌드
./gradlew build

# 단일 서비스 빌드
./gradlew :services-queue:build

# 서비스 로컬 실행
./gradlew :services-auth:bootRun

# 빌드 아티팩트 정리
./gradlew clean
```

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 단일 서비스 테스트 실행
./gradlew :services-queue:test

# 단일 테스트 클래스 실행
./gradlew :services-queue:test --tests "com.yourssu.morupark.queue.business.QueueServiceTest"
```

테스트는 JUnit 5 + MockK를 사용합니다. Kafka 의존 테스트는 `spring-kafka-test`를 사용합니다.

## 아키텍처

### 요청 흐름

```
클라이언트
  → API Gateway (JWT 인증, userId/phoneNumber 헤더 주입)
    → Auth Service    (POST /api/auth/**)
    → Queue Service   (POST /api/queues/**)
```

### 대기열 처리 흐름 (메인 비동기 사이클)

클라이언트가 대기열에 등록한 이후, 아래 사이클이 `queue.processing-interval` 주기로 반복됩니다.

```
[1] StatusOperator (스케줄러)
    - Redis ZSet에서 최대 maxSize명 pop
    - 상태를 PROCESSING으로 저장
    - Kafka "WAITING" 토픽으로 produce
          ↓
[2] Goods Service (TicketProcessRequestConsumer)
    - Kafka "WAITING" consume
    - 추첨 처리 (당첨 / 낙첨 판정)
    - 당첨자 MySQL 저장
    - 결과를 Kafka "TICKET_RESULT" 토픽으로 produce
          ↓
[3] Queue Service (TicketResultConsumer)
    - Kafka "TICKET_RESULT" consume
    - Redis에 최종 상태 저장
      - 당첨: waitingToken|SUCCESS
      - 낙첨: waitingToken|FAILED:사유
      - 재고 없음: SOLD_OUT → 잔여 대기자 전원 FAILED 처리
          ↓
[4] 클라이언트 폴링
    - GET /api/queues/status?token={waitingToken}
    - 응답: WAITING(순위/예상대기시간) | PROCESSING | SUCCESS | FAILED
```

**사이클 시간 측정 (예상 대기시간 고도화)**

위 [1] → [3] 구간의 실제 소요시간(라운드트립)을 `CycleTimeTracker`로 측정하여, `WaitingTimeEstimator`의 예상 대기시간 계산에 반영합니다.
- 측정 시작: `StatusOperator`가 배치를 Kafka로 발행한 시점
- 측정 종료: `TicketResultConsumer`가 해당 배치의 마지막 결과를 수신한 시점
- 최근 10회 이동평균을 사용하며, 측정값이 없을 경우 `queue.processing-interval` 설정값으로 fallback

### Kafka 토픽

| 토픽 | Producer | Consumer | 메시지 형식 |
|------|----------|----------|-------------|
| `WAITING` | Queue Service | Goods Service | `waitingToken\|studentId\|phoneNumber` |
| `TICKET_RESULT` | Goods Service | Queue Service | `waitingToken\|SUCCESS` 또는 `waitingToken\|FAILED:사유` 또는 `SOLD_OUT` |

- 재시도: `@RetryableTopic` 지수 백오프 (4회 시도, 기본 1000ms, ×2 배수)
- Dead Letter Topic은 `.dlt` 접미사 사용

### 서비스 레이어 구조

각 서비스는 일관된 4계층 패키지 구조를 따릅니다:

```
application/   — 컨트롤러, 요청/응답 DTO
business/      — 서비스 클래스 (유스케이스, 도메인 로직)
implement/     — 어댑터, Kafka producer/consumer, 도메인 객체
sub/           — 인프라 설정 (KafkaConfig, 예외 핸들러)
```

`services-goods`는 JPA 엔티티와 도메인 객체를 분리하는 `storage/` 레이어도 포함합니다.

### 주요 인프라

- **Redis** — 대기열 상태(순위, 처리 결과); `REDIS_HOST`/`REDIS_PORT`/`REDIS_PASSWORD`로 설정
- **MySQL** — Goods Service의 상품 및 당첨자 영속화; `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`로 설정
- **Kafka** — `KAFKA_BOOTSTRAP_SERVERS`; 운영환경은 GCP Managed Kafka with SASL_SSL + OAuthBearer
- **JWT** — `JWT_SECRET`으로 설정; API Gateway가 검증 후 userId/phoneNumber를 헤더로 전달

### Kubernetes

인프라 매니페스트는 `infra-aws/`(Kustomize)와 `infra-gcp/`(GKE) 하위에 있습니다. CI/CD는 `.github/workflows/image-build-and-upload.yaml`에 있습니다. 서비스는 Alpine 기반 컨테이너(`amazoncorretto:21-alpine`)에서 비루트 사용자(`morupark:1001`)로 실행됩니다.
