# Kafka Ops (Strimzi)

## 대상 리소스
- `infra/k8s/components/kafka/kafka-cluster.yaml`
- `infra/k8s/components/kafka/zookeeper.yaml`
- 오버레이: `infra/k8s/overlays/dev`, `infra/k8s/overlays/prod`

## 변경 전 체크
- 대상 환경(dev/prod) 오버레이 확인
- 토픽/브로커 설정 변경 시 롤링 영향 범위 확인
- consumer lag 확인 계획 수립

## 운영 명령 예시
- `kubectl -n morupark-prod get kafka`
- `kubectl -n morupark-prod describe kafka <cluster-name>`
- `kubectl -n morupark-prod get pods -l strimzi.io/kind=Kafka`

## 원칙
- 운영 변경은 단계적 반영 후 상태 확인
- 데이터 보존 설정(retention, replication) 변경은 사전 리뷰 필수
