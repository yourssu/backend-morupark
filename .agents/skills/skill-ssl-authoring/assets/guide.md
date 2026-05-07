# skill-ssl-authoring/assets 가이드

이 디렉토리는 `skill-ssl-authoring` 스킬에서 재사용하는 정적 자료를 담습니다.
현재는 새 스킬의 `ssl.json`을 시작할 때 사용할 수 있는 템플릿이 들어 있습니다.

## 파일 설명

- `ssl-template.json`
  - 새 스킬의 `ssl.json` 초안을 만들기 위한 기본 템플릿입니다.
  - `skill`, `scheduling`, `structural`, `logical`, `grounding` 같은 최상위 키 구조를 미리 잡아 둡니다.

## 어떻게 사용하면 되나

1. 새 스킬을 만들 때 이 템플릿을 복사해 시작합니다.
2. `replace-me` 형태의 값을 실제 스킬 정보로 바꿉니다.
3. scene, step, grounding이 실제 저장소 근거와 맞는지 검토합니다.

## 수정할 때 팁

- 템플릿은 최대한 범용적으로 유지하는 것이 좋습니다.
- 특정 스킬 전용 내용은 템플릿에 넣기보다 해당 스킬 디렉토리에서 채우는 편이 낫습니다.
