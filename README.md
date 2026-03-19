# Morupark - 티켓 대기열 관리 시스템

선착순 티켓팅 이벤트에서 대기열 관리, 추첨 처리, 결과 통지를 담당하는 MSA 기반 시스템입니다.

## 서비스 구성

| 서비스 | 포트 | 설명 |
|--------|------|------|
| API Gateway | 8000 | 요청 라우팅 및 JWT 검증 |
| Auth Service | 8081 | JWT 토큰 발급 |
| Queue Service | 8082 | Redis 기반 티켓 대기열 관리 |
| Goods Service | 8083 | 추첨 처리 및 당첨자 저장 |

## 기술 스택

- **Language**: Kotlin
- **Framework**: Spring Boot 3.4.1
- **JDK**: OpenJDK 21
- **Database**: MySQL (Goods Service 당첨자 영속화)
- **Cache**: Redis (대기열 상태 관리)
- **Message Queue**: Apache Kafka

## 요청 흐름

```
클라이언트
  → API Gateway (JWT 인증, userId/phoneNumber 헤더 주입)
    → Auth Service    (POST /api/auth/**)
    → Queue Service   (POST /api/queues/**)
```

## 대기열 처리 흐름

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
          ↓
[4] 클라이언트 폴링
    - GET /api/queues/status?token={waitingToken}
    - 응답: WAITING(순위/예상대기시간) | PROCESSING | SUCCESS | FAILED
```

## Kafka 토픽

| 토픽 | Producer | Consumer | 메시지 형식 |
|------|----------|----------|-------------|
| `WAITING` | Queue Service | Goods Service | `waitingToken\|studentId\|phoneNumber` |
| `TICKET_RESULT` | Goods Service | Queue Service | `waitingToken\|SUCCESS` 또는 `waitingToken\|FAILED:LOST` 또는 `SOLD_OUT` |

## 서비스 레이어 구조

```
application/   — 컨트롤러, 요청/응답 DTO
business/      — 서비스 클래스 (유스케이스, 도메인 로직)
implement/     — 어댑터, Kafka producer/consumer, 도메인 객체
sub/           — 인프라 설정 (KafkaConfig, 예외 핸들러)
```

## 환경변수

| 변수 | 설명 |
|------|------|
| `JWT_SECRET` | JWT 서명 키 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Redis 연결 정보 |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | MySQL 연결 정보 |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka 브로커 주소 |

## 빌드 및 실행

```bash
# 전체 빌드
./gradlew build

# 단일 서비스 빌드
./gradlew :services-queue:build

# 서비스 로컬 실행
./gradlew :services-auth:bootRun

# 전체 테스트
./gradlew test

# 단일 서비스 테스트
./gradlew :services-queue:test
```

## 인프라

- Kubernetes 매니페스트: `infra-aws/`(Kustomize), `infra-gcp/`(GKE)
- CI/CD: `.github/workflows/image-build-and-upload.yaml`
- 컨테이너: Alpine 기반 (`amazoncorretto:21-alpine`), 비루트 사용자(`morupark:1001`)
