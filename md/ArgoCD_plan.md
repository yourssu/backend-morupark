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
    argocd-image-updater.argoproj.io/auth.allow-tags: regexp:^v[0-9].*|^prod-.*
    argocd-image-updater.argoproj.io/queue.allow-tags: regexp:^v[0-9].*|^prod-.*
    argocd-image-updater.argoproj.io/api.allow-tags: regexp:^v[0-9].*|^prod-.*
    argocd-image-updater.argoproj.io/goods.allow-tags: regexp:^v[0-9].*|^prod-.*
```

주의:
- `allow-tags`는 팀 태그 규칙에 맞게 조정한다.
- digest 전략을 쓰더라도 소스 태그 선택 기준은 필요하다.

### 3) GitHub Actions 빌드/푸시 정책
- 태그는 고유하고 immutable하게 발급한다.
  - 예: `prod-${{ github.run_number }}-${{ github.sha }}`
- `latest` push는 운영 자동배포 트리거로 사용하지 않는다(선택적으로만 유지).
- 워크플로우는 서비스별 병렬 빌드 가능.

간단 예시:
```yaml
name: build-and-push
on:
  push:
    branches: [ "main" ]
jobs:
  build-auth:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Build and Push
        run: |
          IMAGE=asia-northeast3-docker.pkg.dev/yourssu-morupark/morupark-repo-private/auth-service
          TAG=prod-${GITHUB_RUN_NUMBER}-${GITHUB_SHA}
          docker build -t ${IMAGE}:${TAG} services-auth
          docker push ${IMAGE}:${TAG}
```

## 운영 적용 순서
1. Argo CD Image Updater에 Git push 권한/레지스트리 조회 권한 설정
2. Argo CD `Application` annotation 반영
3. GitHub Actions 태그 정책을 immutable 방식으로 변경
4. 테스트용 소규모 서비스 1개부터 자동 업데이트 검증
5. 전체 서비스로 확대

## 검증 체크리스트
- 새 이미지 push 후 1~5분 내 Git에 이미지 업데이트 커밋 생성
- Argo CD Application `OutOfSync -> Synced` 자동 전환
- `kubectl get deploy -n morupark-prod -o jsonpath=...`에서 이미지 반영 확인
- `kubectl get pods -n morupark-prod -o jsonpath=...`의 `imageID`가 기대 digest와 일치

## 롤백 전략
- Git revert로 이전 이미지 digest/태그로 즉시 복구
- Argo CD 자동 sync로 클러스터가 이전 버전으로 복원
- 장애 시 Image Updater 일시 중지(annotations 제거 또는 updater deployment scale down)

## 보안/안정성 권고
- Image Updater 계정 권한 최소화(읽기/필요 최소 push 권한)
- 프로덕션은 protected branch + required review 정책 유지
- 다중 서비스 동시 업데이트 시 파동 배포(서비스별 순차 반영) 고려
