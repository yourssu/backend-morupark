# GitHub Actions + Argo CD Image Updater 설정안

## 목표
- Artifact Registry에 새 이미지가 push되면 `prod` 배포가 자동으로 갱신되도록 한다.
- `latest` 대신 immutable 기준(권장: digest 또는 버전 태그)으로 운영 재현성을 보장한다.
- 배포 변경 이력을 Git에 남겨 감사/롤백을 단순화한다.

## 권장 아키텍처
- 빌드/푸시: GitHub Actions
- 배포 동기화: Argo CD (GitOps)
- 이미지 자동 반영: Argo CD Image Updater
- 소스 오브 트루스: 본 repo의 `k8s/overlays/prod/kustomization.yaml`

흐름:
1. GitHub Actions가 서비스 이미지를 Artifact Registry에 push
2. Argo CD Image Updater가 새 이미지 태그/다이제스트 감지
3. Image Updater가 Git에 `kustomization.yaml` 수정 커밋
4. Argo CD가 변경 감지 후 `morupark-prod`에 자동 sync

## 사전 조건
- Argo CD 설치 완료, `morupark-prod`용 Argo CD Application 존재
- Argo CD Image Updater 설치 완료
- Image Updater가 Git 저장소 push 가능한 자격 증명 보유
- Image Updater가 Artifact Registry 조회 가능한 자격 증명 보유

## 저장소 변경 가이드

### 1) Kustomization 이미지 필드 정책
- 현재 운영 고정 digest 사용 상태를 유지한다.
- 자동화 정책 2안:
  - A안(권장): Image Updater가 digest pinning으로 갱신
  - B안: semver/날짜+sha 태그로 `newTag` 갱신

### 2) Argo CD Application annotation (예시)
`Application`에 Image Updater annotation을 부여한다.

```yaml
metadata:
  annotations:
    argocd-image-updater.argoproj.io/image-list: |
      auth=asia-northeast3-docker.pkg.dev/yourssu-morupark/morupark-repo-private/auth-service,
      queue=asia-northeast3-docker.pkg.dev/yourssu-morupark/morupark-repo-private/queue-service,
      api=asia-northeast3-docker.pkg.dev/yourssu-morupark/morupark-repo-private/api-gateway,
      goods=asia-northeast3-docker.pkg.dev/yourssu-morupark/morupark-repo-private/goods-service
    argocd-image-updater.argoproj.io/write-back-method: git
    argocd-image-updater.argoproj.io/git-branch: main
    argocd-image-updater.argoproj.io/update-strategy: digest
    argocd-image-updater.argoproj.io/auth.update-strategy: digest
    argocd-image-updater.argoproj.io/queue.update-strategy: digest
    argocd-image-updater.argoproj.io/api.update-strategy: digest
    argocd-image-updater.argoproj.io/goods.update-strategy: digest
    argocd-image-updater.argoproj.io/auth.allow-tags: regexp:^v[0-9].*|^prod-.*|^sha-.*
    argocd-image-updater.argoproj.io/queue.allow-tags: regexp:^v[0-9].*|^prod-.*|^sha-.*
    argocd-image-updater.argoproj.io/api.allow-tags: regexp:^v[0-9].*|^prod-.*|^sha-.*
    argocd-image-updater.argoproj.io/goods.allow-tags: regexp:^v[0-9].*|^prod-.*|^sha-.*
```

주의:
- `allow-tags`는 팀 태그 규칙(`sha-.*`)에 맞게 조정한다.
- digest 전략을 쓰더라도 소스 태그 선택 기준은 필요하다.

### 3) GitHub Actions 빌드/푸시 정책
- 태그는 고유하고 immutable하게 발급한다.
  - 예: `sha-${{ github.sha }}`
- `latest` push는 운영 자동배포 트리거로 사용하지 않는다(선택적으로만 유지).
- 워크플로우는 서비스별 병렬 빌드 가능.

## 운영 적용 순서
1. Argo CD Image Updater에 Git push 권한/레지스트리 조회 권한 설정
2. Argo CD `Application` annotation 반영
3. GitHub Actions 태그 정책을 immutable 방식으로 변경
4. 테스트용 소규모 서비스 1개부터 자동 업데이트 검증
5. 전체 서비스로 확대

---

# 🚀 GKE 클러스터 복구 및 CI/CD 파이프라인 활성화 가이드

클러스터가 삭제된 후 다시 생성(Re-creation)되었을 때, CI/CD를 정상화하기 위한 실행 순서입니다.

## 1. 인프라 기초 복구 (GKE 가동 직후)
- **네임스페이스 및 기본 리소스 배포**:
  ```bash
  kubectl apply -k k8s/overlays/prod
  ```
- **시크릿 동기화 확인**: External Secrets Operator가 정상 작동하여 `morupark-gcp-secrets`가 생성되었는지 확인합니다.
  ```bash
  kubectl get secret morupark-gcp-secrets -n morupark-prod
  ```

## 2. Argo CD 설치 및 설정
- **Argo CD 엔진 설치**:
  ```bash
  kubectl create namespace argocd
  kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
  ```
- **Argo CD Image Updater 설치**:
  ```bash
  kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj-labs/argocd-image-updater/stable/manifests/install.yaml
  ```

## 3. 자격 증명(Credentials) 연동
Argo CD가 외부 시스템과 통신하기 위해 다음 두 가지가 반드시 필요합니다.

### A. GitHub 접근 권한 (Write-back용)
Argo CD가 `main` 브랜치에 `kustomization.yaml`을 수정해서 커밋하려면 권한이 필요합니다.
1. GitHub에서 **Personal Access Token (PAT)** 생성 (권한: `repo` 전체).
2. Argo CD Settings -> Repositories에 해당 레포지토리와 PAT를 등록합니다.

### B. GCP Artifact Registry 접근 권한 (Image 감지용)
Image Updater가 비공개 GCP 리포지토리의 태그 목록을 읽어야 합니다.
- **추천 방식**: GKE Workload Identity를 사용하여 `argocd-image-updater` KSA와 GCP GSA를 연결합니다.
- **임시 방식**: GCP 서비스 계정 키(JSON)를 K8s Secret으로 생성하여 Image Updater 파드에 마운트합니다.

## 4. Argo CD Application 배포
Argo CD에게 무엇을 동기화할지 명령합니다. (어노테이션 포함)
- `k8s/overlays/prod` 경로를 바라보는 `Application` 리소스를 생성하고, 상단 **[2) Argo CD Application annotation]**에 기재된 어노테이션을 모두 적용합니다.

## 5. 파이프라인 테스트 (검증)
1. 로컬에서 코드 수정 후 `git commit -m "ci test" && git push origin main` 실행.
2. GitHub Actions 탭에서 빌드/푸시 성공 확인.
3. 1~5분 후 GitHub 레포지토리의 `k8s/overlays/prod/kustomization.yaml` 파일에 Argo CD에 의해 자동으로 커밋이 생성되었는지 확인.
4. Argo CD UI에서 `OutOfSync` -> `Synced`로 바뀌며 파드가 롤링 업데이트되는지 확인.
