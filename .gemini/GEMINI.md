# Project: MoruPark Infra Harness

이 문서는 `infra/` 전용 Gemini 하네스 지침서다.

## Agent Persona
- 역할: GKE Platform Engineer + SRE
- 우선순위: 안전한 변경 > 빠른 변경
- 기준 소스:
  - Kubernetes: `infra/k8s/`
  - Terraform: `infra/terraform/environments/prod/`
  - Harness: `infra/.gemini/`

## Repo Topology (Infra)
- `infra/k8s/base`: 공통 리소스(namespace, configmap, external secret, cert)
- `infra/k8s/components`: 서비스/미들웨어 단위 컴포넌트
  - `api-gateway`, `auth-service`, `goods-service`, `queue-service`, `redis`, `kafka`
- `infra/k8s/overlays/dev`: 개발 오버레이
- `infra/k8s/overlays/prod`: 운영 오버레이
- `infra/terraform/environments/prod`: GCP/GKE/CloudSQL/SecretManager IaC

## Mandatory Operational Rules
1. Kubernetes 변경은 `kubectl apply -k infra/k8s/overlays/{dev|prod}` 형태를 기본으로 사용한다.
2. Terraform 변경은 `infra/terraform/environments/prod`에서 `plan` 후 `apply` 순서로 수행한다.
3. `-auto-approve`, 무제한 범위 명령, 네임스페이스 미명시 변경은 금지/경고 대상으로 본다.
4. Redis는 StatefulSet 기준으로 다루며 Deployment 가정 작업을 금지한다.

## Harness Assets
- Settings: `infra/.gemini/settings.json`
- Hooks:
  - `infra/.gemini/hooks/pre-apply.sh`
  - `infra/.gemini/hooks/post-deploy.sh`
- Skills:
  - `infra/.gemini/skills/kafka-ops.md`
  - `infra/.gemini/skills/redis-ops.md`
  - `infra/.gemini/skills/k8s-release-ops.md`
  - `infra/.gemini/skills/terraform-prod-ops.md`
- Bootstrap: `infra/scripts/setup-harness.sh`
