# 이 파일의 리소스들은 terraform apply 후 GKE 클러스터가 생성된 다음에 동작합니다.

# Kubernetes Provider가 인증에 사용할 GKE 클러스터 정보 가져오기
data "google_container_cluster" "gke_cluster" {
  name     = google_container_cluster.morupark_gke.name
  location = var.region
  project  = data.google_project.project.project_id
}

# 현재 gcloud 환경의 인증 정보를 가져오기 위한 데이터 소스
data "google_client_config" "default" {}

provider "kubernetes" {
  host  = format("https://%s", data.google_container_cluster.gke_cluster.endpoint)
  token = data.google_client_config.default.access_token
  cluster_ca_certificate = base64decode(
    data.google_container_cluster.gke_cluster.master_auth[0].cluster_ca_certificate
  )
}
