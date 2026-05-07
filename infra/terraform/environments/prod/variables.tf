variable "project_id" {
  description = "GCP project ID"
  type        = string
  default     = "yourssu-morupark"
}

variable "region" {
  description = "GCP region"
  type        = string
  default     = "asia-northeast3"
}

variable "zone" {
  description = "GCP zone"
  type        = string
  default     = "asia-northeast3-a"
}

variable "db_username" {
  description = "Database admin user name"
  type        = string
}

variable "db_password" {
  description = "Cloud SQL root password"
  type        = string
  sensitive   = true
}

variable "admin_key" {
  description = "Admin API key"
  type        = string
  sensitive   = true
}

variable "domain" {
  description = "Service domain"
  type        = string
  default     = "morupark-api.urssu.com"
}

variable "cloudflare_api_token" {
  description = "Cloudflare API token with DNS edit permission."
  type        = string
  sensitive   = true
  default     = null
  nullable    = true
}

variable "cloudflare_zone_id" {
  description = "Cloudflare Zone ID for the domain."
  type        = string
  default     = null
  nullable    = true
}

variable "cloudflare_proxied" {
  description = "Whether to enable Cloudflare proxy for the A record."
  type        = bool
  default     = false
}

variable "authorized_ip_cidrs" {
  description = "CIDR blocks allowed to access GKE control plane"
  type        = list(string)
}

variable "namespace" {
  type    = string
  default = "morupark-prod"
}

variable "GOOGLE_CREDENTIALS" {
  description = "GCP Credentials path or JSON"
  type        = string
  default     = null
  nullable    = true
}
