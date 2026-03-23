resource "google_compute_global_address" "morupark_static_ip" {
  name         = "morupark-static-ip-prod-v2" # ingress.yaml의 이름과 반드시 일치해야 함!
  description  = "Static IP for Morupark Production Ingress"
  address_type = "EXTERNAL"
}


resource "google_compute_managed_ssl_certificate" "morupark_cert" {
  name = "morupark-managed-cert-prod"

  managed {
    domains = [var.domain]
  }
}
