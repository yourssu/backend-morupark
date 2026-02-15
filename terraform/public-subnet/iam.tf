# main.tf에 선언된 google_project 데이터 소스를 사용합니다.

# 1. auth-service 전용 Google 서비스 계정(GSA) 생성
resource "google_service_account" "auth_service_gsa" {
  account_id   = "auth-service-sa" # GSA의 ID
  display_name = "Auth Service GSA"
}

# 2. Kubernetes 서비스 계정(KSA)이 GSA를 사용할 수 있도록 허용 (Workload Identity 설정)
# "roles/iam.workloadIdentityUser" 역할 부여
resource "google_service_account_iam_member" "workload_identity_user" {
  service_account_id = google_service_account.auth_service_gsa.name
  role               = "roles/iam.workloadIdentityUser"
  member             = "serviceAccount:${data.google_project.project.project_id}.svc.id.goog[morupark-dev/auth-service-sa]"
}

# 3. 생성된 GSA에 Cloud SQL 접속 권한 부여
resource "google_project_iam_member" "sql_client_binding" {
  project = data.google_project.project.project_id
  role    = "roles/cloudsql.client"
  member  = google_service_account.auth_service_gsa.member
}
