# VPC 네트워크 생성
resource "google_compute_network" "morupark_public_vpc" {
  name                    = "morupark-public-vpc"
  auto_create_subnetworks = false
}

# Public 서브넷 생성
resource "google_compute_subnetwork" "public_subnet" {
  name          = "morupark-public-subnet"
  ip_cidr_range = "10.0.1.0/24"
  region        = "asia-northeast3" # 서울 리전
  network       = google_compute_network.morupark_public_vpc.id
  private_ip_google_access = true
}

# 방화벽: 로드밸런서 진입 허용 (80, 443)
resource "google_compute_firewall" "allow_lb" {
  name    = "allow-lb-ingress"
  network = google_compute_network.morupark_public_vpc.name
  allow {
    protocol = "tcp"
    ports    = ["80", "443"]
  }
  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["load-balancer"]
}

# 방화벽: GKE 노드 트래픽 허용 (Auth: 8081, Queue: 8082)
resource "google_compute_firewall" "allow_gke_traffic" {
  name    = "allow-gke-traffic"
  network = google_compute_network.morupark_public_vpc.name
  allow {
    protocol = "tcp"
    ports    = ["8081", "8082"]
  }
  source_ranges = ["130.211.0.0/22", "35.191.0.0/16"] # Google LB 범위
  target_tags   = ["gke-node"]
}

# 방화벽: IAP를 통한 SSH 허용
resource "google_compute_firewall" "allow_ssh" {
  name    = "allow-ssh-iap"
  network = google_compute_network.morupark_public_vpc.name
  allow {
    protocol = "tcp"
    ports    = ["22"]
  }
  source_ranges = ["35.235.240.0/20"]
}