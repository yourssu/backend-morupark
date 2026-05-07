# K8s Release Ops

## 표준 배포 경로
- 개발: `infra/k8s/overlays/dev`
- 운영: `infra/k8s/overlays/prod`

## 권장 순서
1. `kubectl diff -k infra/k8s/overlays/<env>`
2. `kubectl apply -k infra/k8s/overlays/<env>`
3. `infra/.gemini/hooks/post-deploy.sh <namespace>`

## 체크 포인트
- `namespace: morupark-prod` 정합성
- 이미지 태그(dev-latest vs latest) 환경 일치
- HPA patch 대상(`auth-service-hpa`, `queue-service-hpa`, `goods-service-hpa`) 일치
