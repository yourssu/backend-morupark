# Recurring Issues

이 저장소에서 반복적으로 드러난 운영 이슈는 대체로 아래 범주로 모입니다.

## ESO/Secret 동기화

- CRD 부재
- ESO 선행 설치 누락
- `morupark-gcp-secrets` 미생성
- Secret 의존 Pod 기동 실패

## 재프로비저닝 이후 인계 누락

- kubeconfig 갱신 누락
- Terraform 성공 후 K8s 레이어 미복구
- ESO 재설치 순서 누락

## K8s 배포 검증 부족

- `ImagePullBackOff`
- readiness/liveness probe 불일치
- HPA 이름 불일치
- Redis를 Deployment처럼 다루는 오판
