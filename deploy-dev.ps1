# deploy-dev.ps1 - Morupark 원클릭 전체 배포 스크립트

Write-Host "1. Terraform 초기화 및 인프라 배포..." -ForegroundColor Green
terraform -chdir="./terraform/environments/prod" init
terraform -chdir="./terraform/environments/prod" apply -auto-approve

Write-Host "2. kubeconfig 업데이트 (DNS 기반 엔드포인트 활용)..." -ForegroundColor Green
# DNS 엔드포인트를 통해 클러스터에 연결함으로써 IP 변경에 유연하게 대응합니다.
gcloud container clusters get-credentials morupark-gke-prod --region asia-northeast3 --project yourssu-morupark

Write-Host "3. External Secrets Operator (ESO) 설치..." -ForegroundColor Green
helm repo add external-secrets https://charts.external-secrets.io
helm repo update
helm upgrade --install external-secrets external-secrets/external-secrets `
    -n external-secrets --create-namespace `
    --set installCRDs=true `
    --set "serviceAccount.annotations.iam\.gke\.io/gcp-service-account=eso-sa-prod@yourssu-morupark.iam.gserviceaccount.com"

Write-Host "4. Cloud SQL 데이터베이스(moruparkdb_prod) 생성..." -ForegroundColor Green
# 인프라 apply 직후, K8s 리소스 배포 전에 DB 환경을 미리 준비합니다.
python script/create_moruparkdb_prod.py

Write-Host "5. ESO Webhook 및 CRD 대기 (60초)..." -ForegroundColor Yellow
# GKE Autopilot 환경에서 Webhook 포드가 Ready가 될 때까지 기다립니다.
Start-Sleep -Seconds 60

Write-Host "6. Kubernetes 애플리케이션, 시크릿 및 인증서 매니페스트 배포..." -ForegroundColor Green
# Kafka(In-cluster), Redis, API Gateway, Auth, Queue, Goods 서비스와 Ingress 및 ManagedCertificate를 배포합니다.
kubectl apply -k k8s/overlays/prod

Write-Host "7. 시크릿 동기화 및 Kafka 구동 대기 (30초)..." -ForegroundColor Yellow
# ExternalSecret이 GCP에서 값을 가져오고, Kafka 포드가 초기화될 시간을 줍니다.
Start-Sleep -Seconds 30

Write-Host "8. 서비스 전체 롤아웃 재시작..." -ForegroundColor Green
# 시크릿 미찾음 오류나 초기 연결 실패를 방지하기 위해 전체 포드를 재시작합니다.
kubectl rollout restart deployment -n morupark-prod
kubectl rollout restart statefulset -n morupark-prod

Write-Host "9. 고정 외부 IP 및 도메인 동기화 확인 (Cloudflare)..." -ForegroundColor Green
# Ingress가 사용하는 고정 IP 리소스를 확인합니다.
$staticIp = gcloud compute addresses describe morupark-static-ip-prod --global --project yourssu-morupark --format="value(address)"
Write-Host "현재 고정 외부 IP: $staticIp" -ForegroundColor Cyan
Write-Host "Cloudflare(api.morupark.urssu.com)의 A 레코드를 위 IP로 업데이트했는지 확인하세요." -ForegroundColor Yellow

Write-Host "모든 배포 과정이 성공적으로 완료되었습니다!" -ForegroundColor Green
Write-Host "서비스 상태 확인: kubectl get pods -n morupark-prod" -ForegroundColor Cyan
