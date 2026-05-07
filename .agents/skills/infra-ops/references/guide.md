# infra-ops/references 가이드

이 디렉토리는 `infra-ops` 스킬의 세부 참조 문서를 모아둔 곳입니다.
핵심 개요는 `SKILL.md`에 두고, 길고 구체적인 절차는 여기로 분리해 관리합니다.

## 파일 설명

- `terraform-workflow.md`
  - `infra/terraform/environments/prod` 기준의 Terraform 작업 흐름을 설명합니다.
  - 어떤 변수와 민감 정보가 중요한지, 어떤 순서로 `fmt`, `validate`, `plan`, `apply`를 진행해야 하는지 정리합니다.
- `ops-followups.md`
  - Terraform 작업 후 이어질 수 있는 후속 운영 작업을 설명합니다.
  - DB 생성, Secret/IAM 확인, DNS/Ingress 인계, Kubernetes overlay 점검 같은 내용을 다룹니다.

## 어떻게 사용하면 되나

- Terraform 변경 전 점검이나 계획 수립이 필요하면 `terraform-workflow.md`를 먼저 봅니다.
- 인프라 생성 후 해야 할 일을 정리하거나 확인할 때는 `ops-followups.md`를 봅니다.
- 두 문서는 "긴 설명 보관소" 역할이므로, 핵심 사용 흐름은 상위의 `SKILL.md`와 함께 보는 것이 좋습니다.

## 수정할 때 팁

- 실제 저장소 경로와 명령어를 최대한 정확하게 적는 것이 중요합니다.
- 운영 환경 기준 문서이므로, 단순 예시보다 현재 저장소 구조와 맞는 안내를 유지해야 합니다.
