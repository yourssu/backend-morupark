# eso-recurrence-prevention 가이드

이 디렉토리는 External Secrets Operator 장애의 재발을 막기 위한 전용 스킬입니다.
특히 CRD 부재, ESO 선행 설치 누락, 잘못된 배포 순서 때문에 같은 장애가 반복되는 상황을 다룹니다.

## 구성

- `SKILL.md`: 사람이 읽는 핵심 절차
- `ssl.json`: 구조화된 실행 흐름
- `agents/openai.yaml`: 표시 이름과 기본 호출 문구
- `references/`: 원인, 복구 순서, 재발 방지 게이트

## 사용할 때

- `ExternalSecret`/`SecretStore` 관련 에러가 났을 때
- `morupark-gcp-secrets`가 생성되지 않을 때
- 클러스터 재구축 뒤 ESO 순서를 다시 점검해야 할 때
