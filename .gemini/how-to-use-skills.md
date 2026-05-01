infra로 이동
cd infra
하네스 활성화(한 번)
chmod +x scripts/setup-harness.sh
./scripts/setup-harness.sh
Gemini CLI 실행
infra/GEMINI.md + infra/gemini-extension.json을 통해 스킬/훅이 로드됩니다.
스킬을 프롬프트에서 명시 호출
예시:
k8s-release-ops 기준으로 prod 배포 절차대로 진행해줘
terraform-prod-ops 절차로 plan부터 검증해줘
redis-ops 기준으로 redis 상태 점검해줘
kafka-ops 기준으로 kafka 클러스터 점검해줘
훅 동작