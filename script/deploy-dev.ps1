# deploy-dev.ps1

Write-Host "1. Terraform 초기화 및 플랜 생성..." -ForegroundColor Green
terraform -chdir="./terraform/public-subnet" init
terraform -chdir="./terraform/public-subnet" plan

Write-Host "2. Terraform을 사용하여 인프라를 배포합니다..." -ForegroundColor Green
# -auto-approve 옵션은 확인 질문 없이 바로 실행합니다.
terraform -chdir="./terraform/public-subnet" apply -auto-approve

Write-Host "3. kubeconfig 파일을 최신 클러스터 정보로 업데이트합니다..." -ForegroundColor Green
gcloud container clusters get-credentials morupark-gke-public --region asia-northeast3 --project yourssu-morupark

Write-Host "4. Kubernetes에 애플리케이션을 배포합니다..." -ForegroundColor Green
kubectl apply -k infra-gcp/overlays/dev

Write-Host "모든 배포 과정이 완료되었습니다!" -ForegroundColor Green