# GKE Autopilot 모드로 클러스터 생성
resource "google_container_cluster" "morupark_gke" {
  name     = "morupark-gke-public"
  location = var.region
  enable_autopilot = true
  network    = google_compute_network.morupark_public_vpc.id
  subnetwork = google_compute_subnetwork.public_subnet.id

  # 노드가 공인 IP를 가짐으로써 클러스터가 외부와 직접 통신
  networking_mode = "VPC_NATIVE"

  workload_identity_config {
    workload_pool = "${data.google_project.project.project_id}.svc.id.goog"
  }
  release_channel {
    channel = "REGULAR"
  }
  master_authorized_networks_config {
    dynamic "cidr_blocks" {
      for_each = toset(var.authorized_ip_cidrs)
      content {
        cidr_block   = cidr_blocks.value
        display_name = "Managed by Terraform"
      }
    }
  }
  deletion_protection = false
}