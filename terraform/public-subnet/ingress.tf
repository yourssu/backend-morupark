resource "google_compute_global_address" "morupark_static_ip" {
  name = "morupark-static-ip"
}

resource "google_compute_managed_ssl_certificate" "morupark_cert" {
  name = "morupark-managed-cert"

  managed {
    domains = [var.domain]
  }
}

output "ingress_ip" {
  value = google_compute_global_address.morupark_static_ip.address
}