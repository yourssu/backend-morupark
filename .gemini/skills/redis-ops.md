# Redis Ops (StatefulSet)

## 대상 리소스
- `infra/k8s/components/redis/statefulset.yaml`
- `infra/k8s/components/redis/service.yaml`
- `infra/k8s/components/redis/configmap.yaml`

## 핵심 사실
- 이 저장소 Redis는 Deployment가 아니라 StatefulSet이다.
- 변경 시 PVC/재시작/암호 주입 경로를 함께 검토해야 한다.

## 운영 명령 예시
- `kubectl -n morupark-prod get statefulset redis`
- `kubectl -n morupark-prod describe statefulset redis`
- `kubectl -n morupark-prod exec -it redis-0 -- redis-cli INFO replication`

## 원칙
- 리소스 튜닝은 요청/제한을 같이 조정
- 비밀번호/시크릿 경로 변경 시 External Secret 반영 상태 먼저 검증
