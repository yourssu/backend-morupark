output "ingress_static_ip" {
  value       = google_compute_global_address.morupark_static_ip.address
  description = "The public IP address to put in Cloudflare"
}
