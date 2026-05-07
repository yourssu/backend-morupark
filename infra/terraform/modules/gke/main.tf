resource "google_container_cluster" "this" {
  provider         = google-beta
  name             = var.cluster_name
  location         = var.region
  enable_autopilot = true
  network          = var.network_id
  subnetwork       = var.subnetwork_id
  networking_mode  = "VPC_NATIVE"

  workload_identity_config {
    workload_pool = "${var.project_id}.svc.id.goog"
  }

  addons_config {
    http_load_balancing {
      disabled = false
    }
  }

  release_channel {
    channel = "REGULAR"
  }

  control_plane_endpoints_config {
    dns_endpoint_config {
      allow_external_traffic    = true
      enable_k8s_certs_via_dns  = true
      enable_k8s_tokens_via_dns = true
    }
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
