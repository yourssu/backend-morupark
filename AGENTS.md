# Agents Overview

이 저장소의 에이전트 스킬은 `.agents/skills/` 아래에서 관리합니다.
인프라 재프로비저닝, ESO 재발 방지, Kubernetes 배포 검증처럼 운영 중 반복되는 흐름은 `harness` 문서 묶음으로 정리되어 있습니다.

## 시작 위치

- 상위 개요: [docs/harness/README.md](docs/harness/README.md)
- 메타 스킬: [.agents/skills/harness/SKILL.md](.agents/skills/harness/SKILL.md)

## 핵심 스킬

- `eso-recurrence-prevention`
- `cluster-reprovision-handoff`
- `k8s-release-verification`
- `infra-ops`

## 원칙

- 실제 작업 스킬은 `.agents/skills/<skill-name>/`에 둡니다.
- 상위 운영 묶음 설명은 `docs/harness/`에 둡니다.
- 재발 방지 규칙, 점검 순서, 반복 이슈 요약은 문서로 남기고 스킬에서 참조합니다.
