# Morupark 인프라 및 서비스 재시작 가이드 (Restart Guide)

본 문서는 `terraform destroy` 이후 인프라를 처음부터 다시 구축하거나, 클러스터 초기화 시 수행해야 하는 표준 운영 절차를 설명합니다.

---

## 아키텍처 원칙: 역할 분리 (Separation of Concerns)
- **Terraform**: GCP의 물리적/논리적 인프라(VPC, GKE 클러스터, IAM, Cloud SQL)를 담당합니다.
- **Helm**: 쿠버네티스 내부의 인프라 성격의 도구(External Secrets Operator)를 담당합니다.
- **Kustomize (kubectl)**: 비즈니스 애플리케이션 리소스를 담당합니다.

---

## [Step 1] 인프라 기초 공사 (Terraform)
가장 먼저 GCP의 기본 리소스를 생성합니다.

```bash
cd terraform/environments/prod
terraform init
terraform apply -auto-approve
```

### 왜 이 단계가 필요한가요?
- **VPC 및 Subnet**: 모든 리소스가 담길 네트워크 공간을 만듭니다.
- **GKE Cluster**: 컨테이너가 구동될 엔진을 준비합니다.
- **IAM (Workload Identity)**: 쿠버네티스의 서비스 계정(KSA)이 GCP의 리소스(Secret Manager 등)에 접근할 수 있는 "신분증" 역할을 하는 GSA를 미리 생성하고 권한을 부여합니다.
- **Cloud SQL**: 데이터베이스 인스턴스를 준비합니다.

---

## [Step 2] 클러스터 연결 설정 (gcloud)
Terraform이 만든 GKE 클러스터에 명령을 내릴 수 있도록 `kubectl` 권한을 가져옵니다.

```bash
gcloud container clusters get-credentials morupark-gke-prod --region asia-northeast3 --project yourssu-morupark
```

### 왜 이 단계가 필요한가요?
- `terraform apply` 직후에는 로컬의 `kubeconfig`가 새로 생성된 클러스터를 가리키지 않습니다. 이 명령어를 통해 "나의 kubectl 명령은 이제 이 클러스터로 보낸다"라고 설정하는 것입니다.

---

## [Step 3] 시크릿 관리 도구 설치 (Helm)
애플리케이션이 DB 비밀번호나 API 키를 안전하게 가져올 수 있도록 **External Secrets Operator (ESO)**를 설치합니다.

```bash
# Helm 레포지토리 추가 및 업데이트
helm repo add external-secrets https://charts.external-secrets.io
helm repo update

# ESO 설치 (Workload Identity 연결 포함)
helm upgrade --install external-secrets external-secrets/external-secrets \
    -n external-secrets --create-namespace \
    --set installCRDs=true \
    --set serviceAccount.annotations."iam.gke.io/gcp-service-account"="eso-sa-prod@yourssu-morupark.iam.gserviceaccount.com"
```

### 왜 이 단계가 필요한가요?
- **보안**: 쿠버네티스 시크릿에 민감한 정보를 직접 하드코딩하지 않기 위함입니다.
- **자동 동기화**: GCP Secret Manager에 등록된 최신 비밀번호를 ESO가 자동으로 쿠버네티스 내부로 가져옵니다.
- **Workload Identity 연동**: `--set serviceAccount.annotations...` 부분은 ESO가 GCP Secret Manager를 열어볼 수 있는 열쇠(GSA)를 건네주는 중요한 설정입니다.

---

## [Step 4] 애플리케이션 및 시크릿 매니페스트 배포 (Kustomize)
이제 실제 서비스와 시크릿 연결 정보를 배포합니다.

```bash
kubectl apply -k k8s/overlays/prod
```

### 왜 이 단계가 필요한가요?
- **SecretStore & ExternalSecret**: "어떤 GCP 프로젝트의 어떤 시크릿을 가져올 것인가"에 대한 정의를 배포합니다.
- **Deployments & Services**: 실제 비즈니스 로직(Auth, Goods, Queue 서비스 등)이 담긴 컨테이너를 구동합니다.

---

## [Step 5] 상태 점검 (Verification)
모든 것이 정상인지 확인합니다.

```bash
# 1. 시크릿 동기화 상태 확인 (READY가 True여야 함)
kubectl get externalsecret -n morupark-prod

# 2. 실제 쿠버네티스 시크릿 생성 확인
kubectl get secret morupark-gcp-secrets -n morupark-prod

# 3. 애플리케이션 포드 상태 확인
kubectl get pods -n morupark-prod
```

### 문제가 발생한다면?
- 만약 `ExternalSecret`이 `Sync Error`라면, Step 3의 GSA 어노테이션이 정확한지, Step 1에서 Terraform이 IAM 권한(`roles/secretmanager.secretAccessor`)을 제대로 부여했는지 확인해야 합니다.
- 만약 포드가 `CreateContainerConfigError` 상태라면, 시크릿이 생성되기 전에 포드가 먼저 배포된 경우입니다. 아래 명령어로 포드를 재시작하세요.
  ```bash
  kubectl rollout restart deployment -n morupark-prod
  kubectl rollout restart statefulset -n morupark-prod
  ```

---

## 📡 고정 IP 및 DNS 설정 가이드

### 1. 고정 IP(Static IP) 이름 확인
Ingress 설정에 `morupark-static-ip-prod`라는 고정 IP 이름을 쓰고 있습니다. 이 이름이 GCP 콘솔에 등록된 **고정 외부 IP의 이름**과 정확히 일치하는지 확인해야 합니다.

```bash
gcloud compute addresses list --project=yourssu-morupark --filter="name:morupark-static-ip-prod"
```

### 2. Cloudflare DNS 업데이트
결과로 나온 IP(예: 34.160.104.247)를 Cloudflare의 `api.morupark.urssu.com` A 레코드에 등록해야 정상적으로 서비스 접속이 가능합니다.
