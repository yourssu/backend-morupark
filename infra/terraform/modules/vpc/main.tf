resource "google_compute_network" "this" {
  name                    = var.network_name
  auto_create_subnetworks = false
}

resource "google_compute_subnetwork" "this" {
  name                     = var.subnet_name
  ip_cidr_range            = var.subnet_cidr
  region                   = var.region
  network                  = google_compute_network.this.id
  private_ip_google_access = true
}

resource "google_compute_firewall" "allow_lb" {
  name    = var.allow_lb_firewall_name
  network = google_compute_network.this.name
  allow {
    protocol = "tcp"
    ports    = ["80", "443"]
  }
  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["load-balancer"]
}

resource "google_compute_firewall" "allow_gke_traffic" {
  name    = var.allow_gke_firewall_name
  network = google_compute_network.this.name
  allow {
    protocol = "tcp"
    ports    = ["8081", "8082"]
  }
  source_ranges = ["130.211.0.0/22", "35.191.0.0/16"]
  target_tags   = ["gke-node"]
}

resource "google_compute_firewall" "allow_ssh" {
  name    = var.allow_ssh_firewall_name
  network = google_compute_network.this.name
  allow {
    protocol = "tcp"
    ports    = ["22"]
  }
  source_ranges = ["35.235.240.0/20"]
}
