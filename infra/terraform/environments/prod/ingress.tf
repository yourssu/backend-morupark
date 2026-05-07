resource "google_compute_global_address" "morupark_static_ip" {
  name         = "morupark-static-ip-prod-v2"
  description  = "Static IP for Morupark Production Ingress"
  address_type = "EXTERNAL"

  lifecycle {
    prevent_destroy = false
  }
}

resource "google_compute_managed_ssl_certificate" "morupark_cert" {
  name = "morupark-managed-cert-prod"

  managed {
    domains = [var.domain]
  }
}
