# 🚀 Load Test Plan (부하 테스트 계획서)

## 1. 개요
본 부하 테스트는 GKE(Google Kubernetes Engine) 환경에서 구동되는 `morupark-msa` 서비스의 안정성과 확장성을 검증하기 위해 수행됩니다.

### 테스트 목표
- **성능 한계 측정**: 시스템이 처리 가능한 최대 초당 요청 수(RPS) 및 동시 접속자 수 확인.
- **오토스케일링(HPA) 검증**: 부하 증가 시 파드(Pod)가 정상적으로 확장되는지 확인.
- **병목 지점 파악**: API Gateway, Auth, Queue, Goods 서비스 중 병목이 발생하는 구간 식별.
- **복구 능력 확인**: 부하가 줄어들었을 때 시스템이 정상 상태로 복구되는지 확인.

## 2. 테스트 환경
- **Target URL**: `http://api.morupark.urssu.com` (GCP Ingress)
- **Tool**: k6 (JavaScript 기반 부하 테스트 도구)
- **Monitoring Tools**:
  - `kubectl top nodes/pods`: 리소스 사용량 실시간 모니터링
  - `kubectl get hpa`: 오토스케일링 상태 모니터링
  - GCP Cloud Monitoring (옵션)

## 3. 테스트 시나리오

### Scenario A: Baseline Test (기본 부하)
- **목적**: 시스템의 기본적인 성능 지표 측정.
- **조건**: 10명의 가상 사용자(VU)가 2분 동안 지속적으로 요청.
- **기대 결과**: 평균 응답 시간 100ms 이내, 에러 발생률 0%.

### Scenario B: Load Test (점진적 부하)
- **목적**: 예상되는 실사용자 수준에서의 안정성 검증.
- **조건**: 0명에서 200명까지 3분간 VU를 늘리고, 5분간 유지 후 종료.
- **기대 결과**: HPA에 의한 파드 확장 발생, 안정적인 응답 유지.

### Scenario C: Stress Test (임계치 테스트)
- **목적**: 시스템이 파괴되는 지점(Breaking Point) 확인.
- **조건**: 500명 이상의 VU가 동시 접속.
- **기대 결과**: 재고 소진 처리 및 대기열 시스템의 정상 동작 확인.

## 4. 핵심 API 엔드포인트
| API | Method | Path | Description |
|---|---|---|---|
| **Auth** | POST | `/api/auth/login` | 학번/전화번호로 로그인 및 JWT 발급 |
| **Queue** | POST | `/api/queues` | 대기열 진입 요청 (JWT 필요) |
| **Queue Status**| GET | `/api/queues/status` | 대기열 상태 조회 (Token 필요) |

## 5. 실행 및 관찰 방법

### Step 1: 부하 발생 (Terminal 1)
```powershell
k6 run ./load-test/script_???.js
```

### Step 2: 리소스 모니터링 (Terminal 2)
```powershell
while($true) { cls; kubectl top pod -n morupark-prod; sleep 2 }

```

``` bash
while true; do clear; kubectl top pod -n morupark-prod; sleep 2; done
```

### Step 3: HPA 모니터링 (Terminal 3)
```powershell
kubectl get hpa -n morupark-prod -w
```

## 6. 결과 분석 항목
- **RPS (Requests Per Second)**: 초당 처리량
- **Response Time (p95, p99)**: 응답 시간 분포
- **HTTP Error Rate**: 5xx 에러 발생 여부
- **Pod Resource Usage**: CPU/Memory Throttling 여부
- **Queue Wait Time**: 대기열에서 처리까지 소요되는 시간
