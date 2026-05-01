# Deployment YAML 수정 및 상태 최적화 계획

`goods-service`의 안정적인 배포와 상태 확인을 위해 Actuator Probes 경로를 단순화하고 이미지 갱신 정책을 강화합니다.

## Proposed Changes

### [Kubernetes]
#### [MODIFY] [deployment.yaml](file:///C:/Users/yyc/IdeaProjects/backend-morupark/k8s/components/goods-service/deployment.yaml)
- `imagePullPolicy`를 `Always`로 설정 (확인 및 유지)
- `livenessProbe`의 경로를 `/actuator/health`로, 포트를 `8083`으로 수정
- `readinessProbe`의 경로를 `/actuator/health`로, 포트를 `8083`으로 수정 (기존 `/actuator/health/readiness`에서 단순화)
- 이미지 태그를 `:latest`로 변경 (사용자 요청 반영)

### [Documentation]
#### [MODIFY] [workthrough.md](file:///C:/Users/yyc/IdeaProjects/backend-morupark/md/workthrough.md)
- 이번 수정 내역과 그 이유(Review)를 추가 기록합니다.

## Verification Plan

### Automated Tests
- `kubectl diff -f k8s/components/goods-service/deployment.yaml` 명령을 통해 변경 사항이 올바르게 적용되었는지 확인 (드라이 런 형식)

### Manual Verification
- `kubectl apply -f k8s/components/goods-service/deployment.yaml` 적용 후 (필요시)
- `kubectl get pod -n morupark-prod`로 `goods-service` 파드가 `Running` 및 `Ready` 상태인지 확인
- `kubectl describe pod <pod-name> -n morupark-prod`로 Probe 설정이 정상적으로 반영되었는지 확인
# [Goal] GKE 운영 환경 시크릿 동기화 복구 및 ESO 멱등성 검증 완료

현재 `morupark-prod` 네임스페이스의 모든 파드들이 `morupark-gcp-secrets` 시크릿을 찾지 못해 실행되지 않고 있습니다. 이는 idempotency 테스트를 위해 ESO 관련 리소스(GSA, IAM, KSA)를 의도적으로 삭제했기 때문입니다. 이를 복구하여 시스템을 정상화합니다.

## Proposed Changes

### [Cloud Infrastructure] Terraform 복구
- **`terraform/environments/prod`**: `terraform apply`를 실행하여 삭제된 `eso-sa-prod` GSA와 관련된 IAM 바인딩(`roles/secretmanager.secretAccessor`, `roles/iam.workloadIdentityUser`)을 복구합니다.
- **이유 (Reviewer Role)**: ESO가 GCP Secret Manager에 안전하게 접근하기 위해서는 Workload Identity 설정된 GSA가 반드시 필요하기 때문입니다.

### [Kubernetes] External Secrets Operator (ESO) 재설치
- **Helm 설치**: 다음 명령어를 사용하여 ESO를 다시 설치합니다.
  ```bash
  helm upgrade --install external-secrets external-secrets/external-secrets \
      -n external-secrets --create-namespace \
      --set installCRDs=true \
      --set serviceAccount.annotations."iam.gke.io/gcp-service-account"="eso-sa-prod@yourssu-morupark.iam.gserviceaccount.com"
  ```
- **이유 (Reviewer Role)**: CRD와 Webhook이 포함된 전체 오퍼레이터를 복구해야 `ExternalSecret` 리소스가 정상적으로 처리될 수 있습니다. 어노테이션을 통해 Workload Identity를 즉시 연결합니다.

### [Kubernetes] 리소스 재배포 및 동기화 확인
- **Kustomize 적용**: `kubectl apply -k k8s/overlays/prod`를 실행하여 누락되었거나 동기화 대기 중인 리소스를 갱신합니다.

## Verification Plan
1. `kubectl get pods -n external-secrets` 확인
2. `kubectl get secret morupark-gcp-secrets -n morupark-prod` 확인
3. `kubectl get pods -n morupark-prod` 확인
# External Secrets Operator 인증 리팩토링 계획서

ESO(External Secrets Operator)가 Google Secret Manager에 접근할 때 마이크로서비스용 계정이 아닌, 전용 서비스 계정(`external-secrets-sa`)을 사용하도록 인증 구조를 개선합니다.

## 제안된 변경 사항

### [Terraform] `terraform/environments/prod`

#### [MODIFY] [secret_manager.tf](file:///c:/Users/yyc/IdeaProjects/backend-morupark/terraform/environments/prod/secret_manager.tf)
- `external-secrets-sa` GSA 리소스를 복구합니다.
- 해당 GSA에 `roles/secretmanager.secretAccessor` 권한을 부여합니다.
- `external-secrets` 네임스페이스의 `external-secrets` KSA와 Workload Identity 바인딩을 설정합니다.
- 불필요해진 `morupark_sa_secret_accessor` 바인딩을 제거합니다.

### [Kubernetes Manifests] `k8s`

#### [MODIFY] [secret-store.yaml](file:///c:/Users/yyc/IdeaProjects/backend-morupark/k8s/base/secret-store.yaml)
- `spec.provider.gcpsm.auth.workloadIdentity.serviceAccountRef.name`을 `external-secrets`로 변경합니다.
- **Review (Why)**: ESO는 자신의 네임스페이스에 있는 KSA를 통해 인증하는 것이 표준이며, 이를 통해 보안 경계를 명확히 하고 관리를 단순화합니다.

#### [MODIFY] [sa-patch.yaml](file:///c:/Users/yyc/IdeaProjects/backend-morupark/k8s/overlays/prod/sa-patch.yaml)
- 불필요한 `morupark-sa` 관련 패치 블록을 제거합니다.

## 검증 계획

### 자동화 및 수동 검증
1. `terraform apply` 실행 후 GSA 및 IAM 설정 확인.
2. `kubectl annotate sa external-secrets ...` 실행 (사용자 가이드 제공).
3. `kubectl rollout restart deployment external-secrets -n external-secrets` 실행.
4. `kubectl get externalsecret -n morupark-prod`에서 `READY: True` 확인.
