# 프로젝트 워크스루 (Walkthrough)

이 문서는 AI 맥가이버 허브(AI MacGyver Hub)의 백엔드 시스템인 `backend-morupark`의 개발 및 복구 과정을 기록합니다.

## 🚀 프로젝트 개요
전 세계 사용자를 대상으로 50개 이상의 AI 유틸리티 도구를 제공하는 플랫폼의 백엔드로, GKE(Google Kubernetes Engine) 및 Managed Kafka, Redis 등을 활용한 MSA 구조로 설계되었습니다.

## 📅 주요 마일스톤 및 작업 내역

### 1. 인프라 구축 및 설정 (2026-02-24)
- **Terraform (Prod)**: 운영 환경을 위한 퍼블릭 서브넷, DB(g1-small), Kafka 클러스터 및 IAM 권한 설정 완료.
- **K8s 기반 설정**: `infra-gcp`의 Kustomize 설정을 `k8s` 디렉토리로 통합하고 `overlays/prod` 환경 구축.

### 2. 서비스 고도화 및 Managed Kafka 연동 (2026-03-03)
- **Auth & Queue 서비스**: GCP Managed Kafka 연동을 위한 `spring-kafka` 및 `spring-cloud-gcp-starter` 설정.
- **보안**: Workload Identity를 통한 Kafka 인증 핸들러(`GcpLoginCallbackHandler`) 적용.

### 3. 보안 및 시크릿 관리 (2026-03-04)
- **External Secrets Operator (ESO)**: GCP Secret Manager와 K8s Secret 동기화를 위해 ESO 도입.
- **Workload Identity**: `external-secrets-sa` GSA와 KSA를 연결하여 보안 강화.

### 4. 클러스터 복구 및 서비스 안정화 (2026-03-05 ~ 03-06)
- **복구 작업**: ESO 재설치 및 `morupark-gcp-secrets` 동기화 확인.
- **상태 점검**: `api-gateway`, `auth`, `goods`, `queue`, `redis` 서비스의 `Running` 상태 확인.
- **Health Check**: Actuator를 활용한 Liveness/Readiness Probe 설정 최적화.

## 🛠️ 현재 진행 중인 이슈: Ingress 및 인증서 설정
- **현상**: `morupark-prod-ingress-v2`에 외부 IP가 할당되지 않음.
- **원인**: `ManagedCertificate`(`morupark-managed-cert-prod`) 리소스 누락으로 인한 GCE Ingress Controller의 LB 생성 지연.
- **해결 방안**: `k8s/base/managed-cert.yaml` 적용 확인 및 네임스페이스 일치 여부 점검.

---
*최종 업데이트: 2026-03-07*
# Walkthrough - Deployment YAML 수정 및 최적화

`goods-service`의 배포 안정성을 높이기 위해 요청하신 대로 Deployment 설정을 수정하고 검증했습니다.

## 변경 사항 요약

### 1. [Kubernetes](file:///C:/Users/yyc/IdeaProjects/backend-morupark/k8s/components/goods-service/deployment.yaml)
- **이미지 태그**: `dev-2` -> `:latest`로 변경
- **이미지 갱신 정책**: `imagePullPolicy: Always` 설정 확인 (갱신 보장)
- **Liveness Probe**: 
  - 경로: `/actuator/health`
  - 포트: `8083` (직접 명시)
- **Readiness Probe**:
  - 경로: `/actuator/health` (기존 `/actuator/health/readiness`에서 단순화)
  - 포트: `8083` (직접 명시)

### 2. [문서 업데이트](file:///C:/Users/yyc/IdeaProjects/backend-morupark/md/workthrough.md)
- `workthrough.md`에 해당 수정 내역(섹션 8)과 그 이유(Review)를 추가하여 이력을 관리했습니다.

## 검증 결과
- `deployment.yaml` 파일 내의 모든 `Probe` 경로와 포트가 요청하신 대로 정확히 반영되었습니다.
- `imagePullPolicy`와 태그 수정으로 가용 최신 이미지 배포가 보장됩니다.

## 향후 권장 사항
- `Actuator` 헬스 체크 결과를 `kubectl get pod -w`를 통해 모니터링하여, `120초`의 초기 지연(`initialDelaySeconds`)이 적절한지 재검토할 수 있습니다.

# Walkthrough: Cluster Restoration and Secret Synchronization

Idempotency 테스트를 위해 삭제되었던 External Secrets Operator(ESO)를 복구하고, `morupark-prod` 네임스페이스의 가동 중단 상태를 해결했습니다.

## 작업 내용

### 1. External Secrets Operator (ESO) 복구
- **Helm 설치**: GKE Autopilot의 IAM 어노테이션을 포함하여 `external-secrets` 오퍼레이터를 재설치했습니다.
- **주요 명령어**:
  ```bash
  helm upgrade --install external-secrets external-secrets/external-secrets \
      -n external-secrets --create-namespace \
      --set installCRDs=true \
      --set serviceAccount.annotations."iam\.gke\.io/gcp-service-account"="eso-sa-prod@yourssu-morupark.iam.gserviceaccount.com"
  ```

### 2. 시크릿 동기화 (`ExternalSecret`)
- `SecretStore` 및 `ExternalSecret` 리소스를 수동으로 적용하여 GCP Secret Manager와의 연결을 복구했습니다.
- `morupark-gcp-secrets` 시크릿이 성공적으로 동기화(`SecretSynced`)된 것을 확인했습니다.

### 3. 애플리케이션 파드 정상화
- `CreateContainerConfigError` 상태의 파드들을 복구하기 위해 `kubectl rollout restart`를 수행했습니다.
- 현재 모든 서비스(`api-gateway`, `auth`, `queue`, `goods`, `redis`)가 `Running` 상태로 전환되었습니다.

## 검증 결과

- **ESO 상태**: `external-secrets` 네임스페이스 내 모든 파드 `Running`
- **시크릿 상태**: `morupark-gcp-secrets` 데이터 4개 동기화 완료
- **애플리케이션 상태**:
  ```text
  NAME                             READY   STATUS    RESTARTS
  api-gateway-78cf4fdfcc-bnlpv     1/1     Running   0
  auth-service-c68c4c44-7sjfc      2/2     Running   0
  goods-service-585d8ddb64-gdntq   2/2     Running   0
  queue-service-6fcd75c558-8qjlv   2/2     Running   0
  redis-0                          1/1     Running   0
  ```

모든 인프라와 애플리케이션 상태가 정상으로 복구되었습니다.
# Workthrough (진행 상황 및 수정 내역)

이 문서는 프로젝트 진행 과정에서 발생한 주요 수정 사항 및 설계 결정 내역을 기록합니다. Rule에 따라 작업 전후의 진행 내용이 업데이트됩니다.
앞으로 무조건 어떤 내용을 수정할 때 작업 일시, 내용, 설정과 왜 그렇게 설정했는지에 대한 설명을 포함한 리뷰어의 역할을 수행한다.

## 1. Terraform 환경 분리 및 프로덕션(Prod) 설정 (완료)
**작업 일시**: 2026-02-24
**작업 내용**:
- `terraform_temp/public-subnet`에 있던 개발용 스크립트를 기반으로 `terraform/environments/prod` 디렉토리를 생성하여 운영 환경용 테라폼을 분리 구성함.
- **주요 설정 (Review - Why)**:
  - `main.tf`: 운영(Prod) 환경의 명확한 상태 분리를 위해 backend 대상 경로를 `terraform/state/prod`로 변경.
  - `db.tf`: 비용 절감과 운영 최소 안정성을 동시에 충족시키기 위해 Cloud SQL 스펙을 가장 낮은 `db-f1-micro`에서 한 단계 높은 `db-g1-small` (1.7GB RAM)로 상향 조정함. (OOM 방지) 또한 단일 영역(`availability_type = "ZONAL"`)으로 설정하여 최저가 스펙 유지.
  - `kafka.tf`: 서버 관리 오버헤드를 줄이기 위해 GCP Managed Service for Apache Kafka(가장 낮은 스펙, vCPU 3, 3GiB) 리소스를 새롭게 도입함.
  - `iam.tf`: 워크로드가 Managed Kafka 클러스터에 접근할 수 있도록 권한(`roles/managedkafka.client`) 부여.

## 2. K8s 리소스 구조 재설계 및 운영(Prod) 환경 커스터마이징 (완료)
**작업 일시**: 2026-02-24
**작업 내용**:
- 기존 `infra-gcp` 구조를 Kustomize 표준에 맞게 새로운 `k8s` 리소스 디렉토리로 복사 및 구성 변경 적용. 개발 환경(`overlays/dev`)은 완전히 동일하게 유지함.
- **주요 설정 (Review - Why)**:
  - `goods-service` 컴포넌트 추가: 코어 로직 추상화를 위해 당첨 확률(5%) 연산 및 트랜잭션 기록을 담당할 K8s Deployment, HPA 리소스를 분리 생성하여, 트래픽 폭주 시 독립적으로 스케일링할 수 있도록 설계함.
  - Managed Kafka 연동 (Prod): 운영 환경(`overlays/prod/kustomization.yaml`)에서는 인-클러스터 Kafka를 더 이상 배포하지 않도록 제거했고, 대신 ConfigMap을 통해 외부 Managed Kafka Bootstrap Server 엔드포인트를 주입함.
  - Redis StatefulSet 변경: 단순 메모리 캐시가 아니라 대기열 순번(`ZSET`) 등을 쥐고 있는 핵심 임시 스토리지이므로, Pod가 교체되거나 재시작될 때 식별자와 데이터 일관성을 지키기 위해 단순 Deployment에서 `StatefulSet` + Headless Service 네트워킹 구조로 변경함.

## 3. Auth 및 Queue 서비스의 GCP Managed Kafka 연동 (완료)
**작업 일시**: 2026-03-03
**작업 내용**:
- `services-auth` 및 `services-queue` 모듈에 GCP Managed Kafka 연동을 위한 의존성 및 접속 설정을 추가함.
- **주요 설정 (Review - Why)**:
  - `build.gradle.kts`: GCP Managed Kafka는 IAM 권한을 이용한 OAUTHBEARER 인증을 사용합니다. 이에 따라 구글에서 제공하는 `com.google.cloud.hosted.kafka:managed-kafka-auth-login-handler:1.0.1` 라이브러리를 추가하여, GKE의 Workload Identity를 통해 안전하게 인증 토큰을 얻어오도록 조치했습니다. Auth 서비스의 경우 현재 Kafka를 쓰지 않고 있으나, 지시에 따라 추후 이벤트 버스로의 확장을 고려해 `spring-kafka`와 함께 의존성을 선제적으로 주입했습니다.
  - `application.yml` (prod 프로필): 실제 클라우드 환경인 `prod` 프로필에서만 SASL_SSL 프로토콜과 `GcpLoginCallbackHandler`를 타도록 `spring.kafka.properties`를 분리하여 구성했습니다. 이를 통해 로컬/개발 환경과 프로덕션 환경의 Kafka 접속 방식을 완벽하게 분리하고 오버헤드를 막았습니다.

## 4. GCP Starter 의존성 버전 명시 (완료)
**작업 일시**: 2026-03-03
**작업 내용**:
- `services-auth` 및 `services-queue` 모듈의 `spring-cloud-gcp-starter` 의존성에서 발생한 버전 누락 오류를 해결함.
- **주요 설정 (Review - Why)**:
  - `build.gradle.kts`: `spring-cloud-gcp-starter`는 Spring Boot 자체 의존성 관리 기능(BOM)에 기본으로 포함되어 있지 않으므로 명시적인 버전 지정이 필요합니다. 이에 호환성이 확보된 `5.1.2` 버전을 명시하여(`com.google.cloud:spring-cloud-gcp-starter:5.1.2`) 빌드가 정상적으로 수행되도록 수정했습니다.

## 5. .gitignore 최적화 및 Goods 서비스 활성화 (완료)
**작업 일시**: 2026-03-04
**작업 내용**:
- 프로젝트 내 불필요한 인프라 폴더 및 실행 파일(.exe), 파이썬 스크립트(.py)를 `.gitignore`에 추가하여 레포지토리를 정리함.
- `k8s/overlays/prod/kustomization.yaml`에서 주석 처리되어 있던 `goods-service` 관련 설정을 해제함.
- **주요 설정 (Review - Why)**:
  - `.gitignore`: `infra-aws`, `infra-gcp` 등 현재 사용하지 않는 플랫폼 설정과 로컬 실행 파일이 커밋되는 것을 방지하여 협업 효율성을 높임.
  - `Kustomization`: `goods-service`는 당첨 로직을 담당하는 핵심 컴포넌트로, 운영 환경 배포를 위해 리소스 및 이미지 패치 설정을 활성화함.


## 6. External Secrets Operator 도입 및 Ingress 설정 최적화 (진행 중)
**작업 일시**: 2026-03-04
**작업 내용**:
- GKE Prod 환경의 시크릿 관리를 위해 External Secrets Operator (ESO)를 Helm으로 설치함.
- Ingress 리소스의 deprecated 어노테이션을 표준 설정으로 변경함.
- **주요 설정 (Review - Why)**:
  - **ESO Webhook 이슈**: Helm 설치 직후 Webhook 파드가 `Ready` 상태가 되기 전 배포를 시도하여 `no endpoints available` 오류가 발생했으나, 파드 상태 확인 후 재배포하는 방식으로 해결 프로세스를 구축함.
  - **Workload Identity 리팩토링 (ESO 전용)**: ESO가 마이크로서비스용 계정이 아닌 전용 GSA(`external-secrets-sa`)를 사용하도록 설계를 개선함. `terraform/environments/prod/secret_manager.tf`에 전용 GSA를 복구하고 `external-secrets` KSA와 바인딩함.
  - **SecretStore 네임스페이스 격리 및 자체 인증**: `SecretStore` 리소스 내부의 `auth` 블록을 완전히 제거하여, ESO가 다른 네임스페이스의 KSA를 참조하려다 실패하는 문제를 해결함. 결과적으로 ESO 파드 자체의 KSA(`external-secrets`)와 연동된 Workload Identity를 자동으로 사용하게 되어 직관적이고 안정적인 시크릿 동기화가 가능해짐.
  - **Terraform K8s Provider 권한 이슈 및 Helm 도입 (멱등성 보장)**: Terraform Cloud(HCP)에서 GKE Control Plane으로 직접 연결을 시도할 때 네트워크 접근 제한(Timeout)이 발생하여 KSA 자동 생성을 실패했습니다. K8s 리소스 관리를 Terraform에서 분리하고, 대신 **Helm을 통한 ESO 설치 시 `--set serviceAccount.annotations...` 플래그를 활용**하여 Workload Identity 연동 어노테이션을 멱등적으로 자동 주입(`helm upgrade --install`)하는 방식으로 아키텍처를 개선했습니다. (이 과정에서 기존 GSA 삭제 복구 제한을 우회하기 위해 GSA 이름을 `eso-sa-prod`로 변경했습니다.)
  - **Ingress (`ingressClassName`)**: `kubernetes.io/ingress.class` 어노테이션은 K8s 1.18부터 deprecated 되었으므로, 향후 버전 업그레이드 및 GKE Autopilot의 표준 준수를 위해 `spec.ingressClassName: "gce"`로 전환함.
## 7. GKE 운영 환경 시크릿 동기화 복구 및 ESO 재설치 (완료)
**작업 일시**: 2026-03-05
**작업 내용**:
- Idempotency 테스트를 위해 삭제되었던 External Secrets Operator(ESO)를 Helm을 통해 재설치하고, 누락된 `SecretStore` 및 `ExternalSecret` 리소스를 복구함.
- **주요 설정 (Review - Why)**:
  - **Helm Annotations**: GKE Autopilot 환경에서 ESO가 Workload Identity를 즉시 사용할 수 있도록 `--set serviceAccount.annotations."iam\.gke\.io/gcp-service-account"=...` 플래그를 사용하여 설치 시점에 어노테이션을 주입함.
  - **수동 리소스 적용**: `kustomize` 적용 시 Webhook 미준비로 인한 오류 발생을 방지하기 위해, ESO 설치 후 `SecretStore`와 `ExternalSecret`을 수동으로 우선 적용하여 시크릿 동기화를 강제함.
  - **Deployment 재시작**: `CreateContainerConfigError` 상태에 빠졌던 파드들이 새로운 시크릿(`morupark-gcp-secrets`)을 인식할 수 있도록 `kubectl rollout restart`를 수행하여 가동 상태를 `Running`으로 전환함.
- **결과**: `morupark-gcp-secrets` 시크릿이 성공적으로 동기화(`SecretSynced`)되었으며, `redis-0`을 포함한 모든 애플리케이션 파드가 정상 구동됨을 확인힘.

## 8. Goods 서비스 배포 설정 최적화 (Probe 및 이미지 정책) (완료)
**작업 일시**: 2026-03-05
**작업 내용**:
- `goods-service`의 상태 확인(Health Check) 로직을 단순화하고, 배포 시 최신 이미지를 보장하도록 설정을 수정함.
- **주요 설정 (Review - Why)**:
  - **이미지 태그 및 정책 (`:latest`, `imagePullPolicy: Always`)**: GKE Autopilot 환경에서 새로운 이미지 빌드가 배포될 때, 노드에 캐싱된 이전 이미지를 사용하지 않고 항상 레지스트리에서 최신 이미지를 가져오도록 강제함.
  - **Actuator Probe 경로 단순화 (`/actuator/health`)**: 상세한 readiness/liveness 확인 대신, Actuator의 기본 헬스 체크 엔드포인트를 사용하여 파드의 가동 여부를 우선적으로 빠르게 확인하도록 변경함. (복잡한 종속성 체크로 인한 지연 방지)
  - **포트 명시 (`8083`)**: `http` 이름 대신 실제 포트 번호(`8083`)를 직접 사용함으로써 설정의 명확성을 높임.

## 9. Queue 상태 조회 401 오탐 제거 및 레이스 차단 (완료)
**작업 일시**: 2026-03-11  
**작업 내용**:
- `queue-service`에서 `InvalidWaitingTokenException(401)`이 실제 미존재 토큰뿐 아니라, `WAITING`에서 막 빠진 직후 상태 반영 타이밍 갭에도 발생할 수 있는 구조를 개선함.
- 상태 전이를 원자화하고(`claimWaitingTokens`), 상태 조회 우선순위를 재정의하여 false 401을 제거하도록 수정함.
- 상태 문자열 호환성(`COMPLETED` 입력)과 운영 관측성(카운터/구조화 로그)을 함께 보강함.

- **주요 변경 (Review - Why)**:
  - **enqueue 시점 상태 공백 제거**
    - 파일: `services-queue/src/main/kotlin/com/yourssu/morupark/queue/implement/QueueAdapter.kt`
    - 변경: `addToWaitingQueue()`에서 대기열(ZSET) 등록과 함께 `queue:status[token]=WAITING`을 즉시 저장하도록 수정.
    - 이유: 토큰 생성 직후 상태 해시가 비어 있으면, 빠른 스케줄링/조회 상황에서 “존재하지만 상태 미기록” 구간이 생겨 401 오탐 가능성이 커짐.

  - **스케줄러 상태 전이 원자화**
    - 파일: `services-queue/src/main/kotlin/com/yourssu/morupark/queue/implement/QueueAdapter.kt`
    - 변경: Lua 스크립트 기반 `claimWaitingTokens(count)` 추가.
      - `ZPOPMIN`으로 토큰 claim
      - WAITING ZSET 제거
      - STATUS 저장(`PROCESSING`)
      - 상태 TTL 키 갱신
      를 하나의 원자 작업으로 처리.
    - 파일: `services-queue/src/main/kotlin/com/yourssu/morupark/queue/implement/StatusOperator.kt`
    - 변경: 기존 `popFromWaitingQueue()` + 루프 내 `saveStatus(PROCESSING)` 패턴 제거, `claimWaitingTokens()`만 사용하도록 전환.
    - 이유: 기존 분리 흐름은 dequeue 후 상태 저장 전 짧은 공백 구간을 만들며, 이 구간에서 status 조회가 들어오면 false 401이 발생할 수 있음.

  - **상태 조회 방어 로직 강화**
    - 파일: `services-queue/src/main/kotlin/com/yourssu/morupark/queue/business/QueueService.kt`
    - 변경:
      - 1순위: ZSET에 있으면 `WAITING` + rank/estimatedWaitSeconds 반환.
      - 2순위: ZSET에 없으면 상태 저장소 조회 후 `PROCESSING|SUCCESS|FAILED` 반환.
      - 호환: `COMPLETED` 수신 시 `SUCCESS`로 alias 매핑.
      - 예외: 상태도 없고 user info도 없을 때만 401.
      - 불일치 보정: 상태가 `WAITING`인데 ZSET 미존재 등 불일치 케이스는 warn 로그를 남기고 `PROCESSING` 보정 응답.
    - 이유: 토큰 생애주기 전이 중 일시적 불일치/지연으로 정상 토큰을 에러 처리하지 않도록 하기 위함.

  - **토큰 단위 TTL 키 도입(운영 기본값)**
    - 파일: `services-queue/src/main/kotlin/com/yourssu/morupark/queue/implement/QueueAdapter.kt`
    - 파일: `services-queue/src/main/resources/application.yml`
    - 변경: `queue.token-ttl-seconds`(기본 86400초) 설정 추가, 상태/유저 정보를 토큰별 TTL 키(`queue:status:{token}`, `queue:user:{token}`)로 함께 저장/조회.
    - 이유: 장기 운영 시 상태/유저 데이터의 무기한 누적을 방지하고, 만료 정책을 운영 환경 변수로 조정 가능하게 하기 위함.

  - **관측성(메트릭/로그) 강화**
    - 파일: `services-queue/src/main/kotlin/com/yourssu/morupark/queue/business/QueueService.kt`
    - 변경:
      - 카운터 추가: `status_miss_after_dequeue`, `invalid_token_true_miss`, `state_inconsistency`
      - 구조화 로그 필드: `token`, `inQueue`, `statusHashValue`, `decision`
    - 이유: “실제 미존재”와 “전이 중 불일치”를 운영 지표에서 분리 관측해 재발 시 즉시 원인 파악 가능.

## 11. CI/CD 파이프라인 구성 및 GCP 통합 (완료)
**작업 일시**: 2026-03-26
**작업 내용**:
- AWS ECR 기반이었던 CI 워크플로우를 GCP Artifact Registry 기반으로 전면 개편하고, ArgoCD Image Updater를 통한 자동 배포 체계를 구축함.
- **ArgoCD_plan.md 업데이트**: 클러스터 복구 후 CI/CD를 활성화하기 위한 7단계 로드맵(ArgoCD 설치, Image Updater 설정, 자격 증명 연동 등)을 문서화함.
- **주요 설정 (Review - Why)**:
  - **CI (GitHub Actions)**:
    - **모듈별 독립 빌드**: `paths-filter`를 사용하여 변경된 서비스만 빌드 및 푸시하도록 최적화함. (불필요한 빌드 비용 절감)
    - **불변 태그 전략**: `latest` 외에 `${{ github.sha }}` 기반의 `sha-{short-sha}` 태그를 강제로 생성하여, 운영 환경 배포 시 어떤 코드 시점에서 빌드되었는지 명확히 추적 가능하게 함.
    - **GCP 인증 통합**: `google-github-actions/auth`를 통해 Artifact Registry에 안전하게 접근하도록 구성함. (비밀번호 대신 단기 토큰 사용 권장)
  - **CD (ArgoCD Image Updater)**:
    - **자동 감지 및 Pull 기반 배포**: CI가 이미지를 푸시하면 ArgoCD가 이를 감지하여 배포를 갱신하는 'Pull' 방식으로 구성하여 클러스터 보안성을 높임 (클러스터가 외부의 요청을 기다리는 것이 아니라 스스로 레지스트리를 확인).
    - **Git Write-back 적용**: 이미지 변경 사항을 `k8s/overlays/prod/kustomization.yaml`에 다시 커밋하게 함으로써 Git 저장소를 인프라의 최종 상태(Source of Truth)로 유지함.

- **향후 과제**:
  - ArgoCD `Application` 리소스에 가이드된 어노테이션을 반영하여 실 배포 연동 확인.
  - `GCP_SA_KEY` 시크릿이 GitHub Repository에 등록되어 있는지 확인 필요.
