# GKE 운영 환경 Pod 배포 오류 (ImagePull & Secret) 해결 계획서

현재 `morupark-prod` 네임스페이스의 Pod들이 `ImagePullBackOff`와 `CreateContainerConfigError` 상태에 빠진 원인과 해결 단계를 상세히 정리했습니다.

## 1. 이슈 원인 분석

### A. `ImagePullBackOff` (auth, goods, queue 서비스)
- **증상:** `failed to pull and unpack image "asia-northeast3-docker.pkg.dev/...`
- **원인:** 신규 생성된 GKE Autopilot 클러스터의 워커 노드들이 비공개 Artifact Registry(`morupark-repo-private`)에서 이미지를 가져올 수 있는 IAM 권한(`roles/artifactregistry.reader`)이 부족합니다. GKE 노드는 기본적으로 Compute Engine Default Service Account를 사용하는데, 이 계정에 권한이 부여되지 않았습니다.

### B. `CreateContainerConfigError` (redis-0)
- **증상:** `secret "morupark-gcp-secrets" not found`
- **원인:** Redis 파드가 필요로 하는 `morupark-gcp-secrets` Secret이 생성되지 않았습니다. Kustomize를 통해 `ExternalSecret` 타입의 리소스를 배포하려 했으나, 현재 GKE 클러스터에 **External Secrets Operator (ESO)** 가(CRD 포함) 설치되어 있지 않아 시크릿 동기화가 이루어지지 않고 있습니다.

---

## 2. 해결 단계별 계획

### Step 1: Terraform을 통한 Artifact Registry 권한 부여
GKE 노드가 이미지를 정상적으로 가져올 수 있도록 Terraform IAM 코드를 수정하고 적용합니다.

#### [MODIFY] `terraform/environments/prod/iam.tf`
다음 코드를 추가하여 Compute Engine Default Service Account에 `roles/artifactregistry.reader` 역할을 부여합니다.
```hcl
resource "google_project_iam_member" "gke_node_ar_reader" {
  project = local.project_id
  role    = "roles/artifactregistry.reader"
  member  = "serviceAccount:${data.google_project.project.number}-compute@developer.gserviceaccount.com"
}
```
* **적용:** `terraform apply`를 실행하여 권한 추가를 완료합니다.

### Step 2: 클러스터에 External Secrets Operator (ESO) 설치
Helm을 이용하여 GKE 클러스터에 ESO를 설치하고 필요한 CRD(`ExternalSecret`, `SecretStore` 등)를 클러스터에 활성화합니다.

* **실행 명령어:**
```bash
helm repo add external-secrets https://charts.external-secrets.io
helm repo update
helm install external-secrets external-secrets/external-secrets \
    -n external-secrets --create-namespace \
    --set installCRDs=true
```

### Step 3: K8s 리소스 재배포 및 상태 확인
CRD가 설치되었으므로, 기존에 누락되었던 ExternalSecret 리소스들이 정상적으로 반영되도록 다시 패치합니다.

* **실행 명령어:**
```bash
kubectl apply -k k8s/overlays/prod
```
* 이후 `kubectl get secret morupark-gcp-secrets -n morupark-prod` 명령어가 정상적으로 GCP Secret Manager로부터 데이터를 조회해왔는지 확인합니다.
* 파드들이 자동으로 재시작되며 정상적인 `Running` 상태가 되는지 모니터링합니다.

---

## 3. 사용자 검토 대기
위 두 가지 핵심 문제(IAM 권한 부여, ESO 헬름 차트 설치)를 진행해야 합니다. 
계획이 승인되면 바로 Terraform 수정 및 Helm 설치를 시작하겠습니다.
