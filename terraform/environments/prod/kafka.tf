# GCP Managed Service for Apache Kafka API 활성화
resource "google_project_service" "kafka_api" {
  project            = var.project_id
  service            = "managedkafka.googleapis.com"
  disable_on_destroy = false
}

# GCP Managed Service for Apache Kafka (최소 스펙)
resource "google_managed_kafka_cluster" "morupark_kafka" {
  depends_on = [google_project_service.kafka_api]
  provider   = google-beta
  cluster_id = "morupark-kafka-prod"
  location   = var.region

  capacity_config {
    vcpu_count   = 3          # GCP Managed Kafka에서 지원하는 최소 vCPU (일반적으로 3)
    memory_bytes = 3221225472 # 3 GiB (최소 메모리 사양)
  }

  gcp_config {
    access_config {
      network_configs {
        subnet = google_compute_subnetwork.public_subnet.id
      }
    }
  }

  labels = {
    env = "prod"
  }
}
