# 쿠버네티스 리소스는 terraform apply 시
# GKE 클러스터와 통신이 필요함.

# Kubernetes Provider가 사용할 GKE 클러스터 정보 가져오기
data "google_container_cluster" "gke_cluster" {
  name     = google_container_cluster.morupark_gke.name
  location = var.region
  project  = data.google_project.project.project_id
}

# 현재 gcloud 인증 정보를 가져오기 위한 데이터 소스
data "google_client_config" "default" {}

provider "kubernetes" {
  host  = format("https://%s", data.google_container_cluster.gke_cluster.endpoint)
  token = data.google_client_config.default.access_token
  cluster_ca_certificate = base64decode(
    data.google_container_cluster.gke_cluster.master_auth[0].cluster_ca_certificate
  )
}

# morupark-gcp-secrets 리소스는 Terraform 관리에서 제외하고 
# External Secrets Operator (ESO)를 통해 관리합니다.
# (이전에 타임아웃 오류를 일으켰던 부분입니다.)

/*
resource "kubernetes_secret_v1" "morupark_gcp_secrets" {
  metadata {
    name      = "morupark-gcp-secrets"
    namespace = var.namespace
  }

  data = {
    # db.tf 와 secret_manager.tf에서 직접 참조
    DB_PASSWORD    = data.google_secret_manager_secret_version.db_password_version.secret_data

    # secret_manager.tf에서 참조 (Terraform으로 관리할 경우)
    JWT_SECRET     = google_secret_manager_secret_version.jwt_secret_version.secret_data
    DB_USERNAME    = google_secret_manager_secret_version.db_username_version.secret_data
    REDIS_PASSWORD = google_secret_manager_secret_version.redis_password_version.secret_data
  }

  type = "Opaque"
}
*/
