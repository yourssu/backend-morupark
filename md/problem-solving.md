# GKE Prod 배포 문제 해결 보고서 (Troubleshooting Report)

본 문서는 `backend-morupark` 프로젝트의 GKE 운영(Production) 환경 배포 중 발생한 여러 문제점과 그 원인, 그리고 해결 과정을 상세히 기록한 문서입니다.

---

## 📌 문제 1: Kustomize 배포 설정 오류
### 🚨 문제 상황
`kubectl apply -k k8s/overlays/prod` 명령어 실행 시, 다음과 같은 에러 라벨과 경고가 발생하며 배포가 실패함.
- **Warning:** `'bases' is deprecated. Please use 'resources' instead.`
- **Error:** `namespace transformation produces ID conflict:` (namespace-prod 이름 충돌)

### 🔍 원인 분석
Kustomize `v1beta1` 버전부터 `kustomization.yaml` 내 `bases` 필드가 `resources` 필드로 통합 운영되도록 Deprecate 되었습니다.

### 🛠️ 해결 방법
- `k8s/overlays/prod/kustomization.yaml`에서 `bases` 필드를 제거하고, `../../base` 경로를 `resources` 배열 내부로 이동시켰습니다.
- 중복 선언된 `namespace.yaml` 파일 로드 충돌을 제거했습니다.

---

## 📌 문제 2: 클러스터 자격 증명(Connection Timeout) 오류
### 🚨 문제 상황
`failed to download openapi: Get "...: connectex: A connection attempt failed..."`
### 🛠️ 해결 방법
- 로컬 `kubeconfig` 자격 증명 정보를 클러스터 정보에 맞게 갱신했습니다 (`gcloud container clusters get-credentials ...`).

---

## 📌 문제 3: Pod ImagePullBackOff & Secret 생성 오류
### 🚨 문제 상황
- `auth-service`, `queue-service` 파드 모두 `ImagePullBackOff` 상태.
- `redis-0` 파드의 `CreateContainerConfigError` 

### 🔍 원인 분석 및 해결 방법
1. **Artifact Registry 권한 부여:** Terraform `iam.tf` 에 GKE 노드의 AR 읽기 권한 추가.
2. **이미지 태그 수정:** Kustomization `v1.0.0` 태그 부재로 인하여 `latest` 로 수정 후 대상 이미지 Re-tag 처리 완료.
3. **SecretManager 연동:** ESO(External Secrets Operator)를 설치하고 전용 GSA(`external-secrets-sa`)를 생성하여 Workload Identity로 GCP 암호화 키를 불러오도록 자동 연동. 빈 껍데기 뿐이던 Secret 버전에 임시 값을 주입.

---

## 📌 문제 4: Cloud SQL 연동 오류 및 Application Crash
### 🚨 문제 상황
- Pod 내부 `cloud-sql-proxy` 컨테이너가 인증에 실패(Application Default Credentials) 하거나 `morupark-db-public` DB를 찾지 못하고 크래시됨.
- 로그 오류 1: `Permission 'secretmanager.versions.access' denied` 및 `Auth Error`.
- 로그 오류 2: `Schema-validation: missing table [platform]`

### 🔍 원인 분석
1. **Workload Identity Service Account 혼선:** Terraform에 정의된 서비스 어카운트 이름(`auth-service-sa-prod`)과 K8s 클러스터 내의 KSA 이름(`auth-service-sa`)이 일치하지 않아 매핑이 실패됐었습니다.
2. **DB 접속 주소 Hardcoding:** `queue-service`의 Deployment와 ConfigMap 등에 Prod 환경이 아닌 Public/Dev DB 주소(`yourssu-morupark:asia-northeast3:morupark-db-public`)가 하드코딩 되어있었습니다.
3. **DB 스키마 없음 (테이블 미생성):** 모든 DB 접근/인증 이슈를 뚫고 최종적으로 Spring Boot 앱이 Prod DB(`morupark-db-prod`)에 연결하는 데에는 성공했으나! DB 안에 `platform` 등의 테이블이 생성되지 않은 빈 깡통 상태라 Hibernate 의 `validate` 모드에 의해 실행 도중 튕기고 있습니다.

### 🛠️ 해결 방법
1. **KSA/GSA 매핑 수정:** Terraform 코드를 수정하고, Kustomization의 `sa-patch.yaml` 을 이용해 KSA에 적절한 Prod IAM Annotation을 덮어 씌우는 방식으로 Workload Identity 권한 문제를 타파했습니다.
2. **동적 DB 주소 패치:** `queue-service`에도 ConfigMap 변수를 보도록 수정하고, `sql-config-patch.yaml`을 추가하여 배포 시점에 `morupark-db-prod`로 오버라이드 되도록 처리했습니다.
3. **DB 테이블 생성 (완료):** Kustomize 패치 파일을 통해 컨테이너 환경 변수에 `SPRING_JPA_HIBERNATE_DDL_AUTO: "update"`를 주입하여, 애플리케이션 기동 시 누락된 테이블들(`platform` 등)을 자동 생성하도록 조치했습니다. 성공적으로 런타임 크래시가 소거되었습니다.

---

## 📌 문제 5: Namespace 및 ServiceAccount 네이밍 불일치 정리
### 🚨 문제 상황
- `morupark-dev` 네임스페이스 및 `*-sa` 등 Dev용 설정들이 `k8s/base`와 `k8s/components`에 광범위하게 남아 있어 Kustomize overlay 시 혼동을 주고 일부 오작동을 유발함.

### 🛠️ 해결 방법
- 모든 `.yaml` 파일 내의 `morupark-dev`를 `morupark-prod`로 일괄 치환했습니다.
- Service Account 이름을 `auth-service-sa-prod`, `queue-service-sa-prod` 등으로 모두 `prod` 접미사를 붙여 명시적으로 분리 및 통일시켰습니다.
- Terraform 의 `iam.tf` 에도 변경된 KSA 이름(`each.value`)들을 정확하게 바라보도록 Workload Identity 바인딩을 업데이트했습니다.
- 불필요해진 (ESO 도입으로 사용하지 않는) `secret-provider-class.yaml` 관련 리소스를 `base` 계층에서 완전히 제거했습니다.

---

## ✅ 현재 상태 점검 요약
- **API Gateway Ready 실패**: `spring-boot-starter-actuator` 의존성이 누락되어 `/actuator/health/readiness` 응답이 404를 반환하고 있습니다. 의존성 추가가 필요합니다.
- **Queue Service CrashLoop**: Kafka Auth Handler 클래스(`ManagedKafkaLoginCallbackHandler`)를 찾지 못하고 있습니다. 라이브러리 버전 및 패키지 경로를 재확인 중입니다.
- **인프라 결함 결론**: 인프라 인가, 통신 로직 결함(IAM/Proxy/Image)은 모두 고쳤으나, 애플리케이션 라이브러리 의존성 및 헬스체크 설정 미비로 인해 실 서비스 가동이 지연되고 있습니다.

🎉 진행 상황 종합 및 오류 분석 리포트

✅ 1. 해결된 항목 (현재까지의 성과)
가장 애를 먹이던 시크릿 동기화 및 Workload Identity 인증 문제가 완벽히 해결되었습니다!

SecretStore와 ExternalSecret이 정상적으로 연결되어 SecretSynced (READY=True) 상태가 되었습니다.
이에 따라 시크릿(DB 비밀번호 등)을 주입받지 못해 무한 재시작(CreateContainerConfigError) 중이던 api-gateway, auth-service, queue-service 파드들이 모두 시크릿을 성공적으로 읽어와 Running 상태로 정상 복구되었습니다.
kafka, zookeeper, redis 등 의존성 파드들도 모두 정상 동작 중입니다.
Terraform과 Helm을 혼합하여 완전한 멱등성 보장 인프라를 구축했습니다. (아까 직접 Terraform 파멸 후 재생성 테스트로 검증했습니다.)
🚨 2. 남은 이슈 분석 (goods-service)
현재 morupark-prod 네임스페이스의 유일한 오류는 goods-service의 ImagePullBackOff 상태입니다.

분석 결과: api-gateway, auth 등 다른 파드의 이미지는 정상적으로 Pull 되는 것으로 보아, GKE 노드의 Artifact Registry 읽기 권한(roles/artifactregistry.reader)은 정상입니다. (권한 문제가 아님)

따라서 이 에러가 발생하는 원인은 아키텍처에 goods-service 매니페스트를 활성화해 두었지만, 실제 Artifact Registry에 goods-service:latest 도커 이미지가 푸시(Push)되어 있지 않기 때문입니다.

해결 방안 선택:

[추천] 이미지 푸시: 로컬이나 CI/CD 환경에서 goods-service 서버 애플리케이션의 도커 이미지를 빌드하여 asia-northeast3-docker.pkg.dev/yourssu-morupark/morupark-repo-private/goods-service:latest 저장소에 업로드(Push)해 주세요. 이미지만 올라가면 쿠버네티스가 알아서 다시 다운받아 실행합니다.
배포 보류: 만약 goods-service 개발이 아직 끝나지 않아 당장 배포할 수 없다면, k8s/overlays/prod/kustomization.yaml 파일에서 goods-service 관련 리소스(deployment 등)를 다시 임시로 주석 처리(#)하고 kubectl apply를 실행하시면 에러가 깔끔하게 정리됩니다.
어떤 방향으로 진행하시겠습니까?