resource "cloudflare_dns_record" "ingress_a_record" {
  count = (
    var.cloudflare_api_token != null && trimspace(var.cloudflare_api_token) != "" &&
    var.cloudflare_zone_id != null && trimspace(var.cloudflare_zone_id) != ""
  ) ? 1 : 0

  zone_id = var.cloudflare_zone_id
  name    = var.domain
  type    = "A"
  content = google_compute_global_address.morupark_static_ip.address
  proxied = var.cloudflare_proxied
  ttl     = 1
}
