# ESO 이슈 분석 및 해결 가이드 (2026-04-30)

## 요약
이번 장애의 핵심 원인은 아래 2가지입니다.
1. `ExternalSecret`, `SecretStore` CRD가 클러스터에 없어 `kubectl apply -k infra/k8s/overlays/prod`가 부분 실패함
2. Terraform로 인프라를 먼저 올린 뒤, ESO(Helm) 설치 전에 K8s 리소스를 적용해 `morupark-gcp-secrets` 생성이 막힘

이로 인해 애플리케이션 Pod가 Secret 의존성을 충족하지 못해 정상 기동이 지연/실패할 수 있는 상태가 발생했습니다.

---

## 실제 증상
- `kubectl apply -k infra/k8s/overlays/prod` 중 아래 에러 발생
  - `no matches for kind "ExternalSecret" in version "external-secrets.io/v1"`
  - `no matches for kind "SecretStore" in version "external-secrets.io/v1"`
- 클러스터 리소스 일부(Deployment/Service/Ingress/HPA)는 생성되지만 Secret 동기화 리소스는 누락됨
- 이후 앱 Pod에서 `morupark-gcp-secrets` 참조 시 `CreateContainerConfigError` 또는 초기화 실패 가능

---

## 근본 원인
1. **CRD 부재**
- ESO를 설치하지 않았거나, `installCRDs=true` 없이 설치해 CRD가 존재하지 않음

2. **배포 순서 위반**
- 정석 순서
  - Terraform (GKE/IAM/네트워크)
  - ESO 설치 (Helm + CRD)
  - Kustomize 배포 (`ExternalSecret`, `SecretStore` 포함)
- 실제는 ESO 이전에 Kustomize를 적용해 CRD 미인식 에러 발생

3. **운영에서 반복되는 이유**
- destroy/recreate 또는 신규 클러스터 재구축 시 CRD는 클러스터 리소스라 초기화됨
- 앱 매니페스트만 먼저 적용하면 동일 문제가 반복됨

---

## 해결 절차 (표준)

### 1) ESO 설치/복구
```bash
helm repo add external-secrets https://charts.external-secrets.io
helm repo update
helm upgrade --install external-secrets external-secrets/external-secrets \
  -n external-secrets --create-namespace \
  --set installCRDs=true \
  --set serviceAccount.annotations."iam\.gke\.io/gcp-service-account"="eso-sa-prod@yourssu-morupark-494902.iam.gserviceaccount.com"
```

### 2) CRD 존재 확인
```bash
kubectl get crd externalsecrets.external-secrets.io secretstores.external-secrets.io
```

### 3) 운영 오버레이 재적용
```bash
kubectl apply -k infra/k8s/overlays/prod
```

### 4) Secret 동기화 확인
```bash
kubectl get externalsecret -n morupark-prod
kubectl get secret morupark-gcp-secrets -n morupark-prod
```

### 5) 워크로드 상태 확인
```bash
kubectl get pods -n morupark-prod
kubectl get deploy,svc,ingress,hpa -n morupark-prod
```

---

## 재발 방지
1. **런북 고정**
- `Terraform -> get-credentials -> ESO Helm -> Kustomize` 순서를 운영 표준으로 고정

2. **사전 게이트 추가**
- 배포 전 아래 검사 강제
  - `kubectl get crd externalsecrets.external-secrets.io`
  - 없으면 앱 배포 중단 후 ESO 설치 단계로 분기

3. **Idempotent 복구 스크립트화**
- ESO 설치는 `helm upgrade --install` 유지

4. **관측 포인트**
- `kubectl get events -n morupark-prod --sort-by=.lastTimestamp`
- `kubectl logs -n external-secrets deploy/external-secrets`

---

## 이번 세션 부가 이슈
- `kubectl` 타임아웃은 kubeconfig 갱신(`gcloud container clusters get-credentials ...`)으로 복구됨
- `authorized_ip_cidrs`는 현재 작업 공인 IP(`175.198.119.106/32`)와 일치
- Terraform 경고: `terraform.tfvars`의 `redis_password`는 선언되지 않은 변수라 정리 필요

---

## 결론
이번 장애는 권한 자체보다 **ESO/CRD 선행 설치 누락으로 인한 배포 순서 문제**입니다.
운영에서는 ESO와 CRD를 먼저 보장한 뒤 Kustomize를 적용해야 하며, 이 절차를 자동화 게이트로 강제하면 동일 장애를 반복 없이 막을 수 있습니다.

---

## 실행 결과 (2026-04-30 실제 반영)
- `eso.md`를 UTF-8로 재작성해 인코딩 깨짐 복구 완료
- `helm upgrade --install external-secrets ... --set installCRDs=true` 실행 완료
- CRD 확인 완료
  - `externalsecrets.external-secrets.io`
  - `secretstores.external-secrets.io`
- `kubectl apply -k infra/k8s/overlays/prod` 재적용 완료
- `SecretStore.projectID`를 `yourssu-morupark-494902`로 수정 반영 완료
- `kubectl annotate externalsecret ... force-sync=...`로 강제 동기화 수행
- 현재 상태
  - `ExternalSecret`: `SecretSynced` / `READY=True`
  - `Secret morupark-gcp-secrets`: 생성 완료(`Opaque`, `DATA=4`)

### 남은 이슈 (ESO 외)
- 일부 앱 Pod가 `ImagePullBackOff`
- 원인: Artifact Registry 이미지 Pull 시 404 (`.../morupark-repo-private/<service>:latest`)
- 즉, ESO/Secret 문제는 해결됐고, 남은 건 이미지 저장소/태그 존재 여부(배포 파이프라인) 문제
