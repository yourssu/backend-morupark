# Morupark - 대기열 시스템 MSA

## 프로젝트 구조

```
morupark/
├── services/
│   ├── common/              # 공통 모듈
│   ├── auth/               # 인증/인가 서비스 (Port: 8081)
│   └── queue/              # 대기열 관리 서비스 (Port: 8082)
├── infra/                  # 인프라 관련 코드
├── .github/workflows/      # CI/CD 파이프라인
└── .env.example           # 환경변수 예시
```

## 기술 스택

- **Language**: Kotlin
- **Framework**: Spring Boot 3.4.1
- **JDK**: OpenJDK 21
- **Database**: MySQL 8.0 (RDS)
- **Cache**: Redis
- **Message Queue**: Apache Kafka
- **Architecture**: 4계층 아키텍처 (Application, Business, Implement, Storage)

## 서비스별 포트

| 서비스 | 포트 | 설명 |
|--------|------|------|
| Auth Service | 8081 | 인증/인가 |
| Queue Service | 8082 | 대기열 관리 |

## API 엔드포인트

### API Gateway
- `GET /auth/**` → Auth Service로 라우팅
- `GET /queue/**` → Queue Service로 라우팅

## 환경별 설정

### 개발 환경 (dev)
- H2 인메모리 데이터베이스 사용
- 로컬 Kafka, Redis 사용

### 운영 환경 (prod)
- MySQL RDS 사용
- 실제 Kafka, Redis 클러스터 사용

## 팀원 가이드

### 새로운 기능 개발 시

1. 해당 서비스의 4계층 구조에 맞게 개발
2. 공통 기능은 `services/common` 모듈에 추가
3. 서비스간 통신은 Kafka 이벤트 사용
4. API 변경 시 API Gateway 라우팅 설정 업데이트

### 테스트 실행
```bash
./gradlew test
```

### 개별 서비스 빌드
```bash
./gradlew :services-{service-name}:build
```
