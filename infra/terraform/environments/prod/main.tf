terraform {
  required_version = ">= 1.0.0"

  cloud {
    organization = "yourssu_ducks"

    workspaces {
      name = "yourssu-morupark"
    }
  }

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 5.14.0"
    }
    google-beta = {
      source  = "hashicorp/google-beta"
      version = ">= 5.14.0"
    }
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = ">= 4.0.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
  zone    = var.zone
  credentials = (
    var.GOOGLE_CREDENTIALS != null && trimspace(var.GOOGLE_CREDENTIALS) != ""
  ) ? var.GOOGLE_CREDENTIALS : null
}

provider "google-beta" {
  project = var.project_id
  region  = var.region
  zone    = var.zone
  credentials = (
    var.GOOGLE_CREDENTIALS != null && trimspace(var.GOOGLE_CREDENTIALS) != ""
  ) ? var.GOOGLE_CREDENTIALS : null
}

provider "cloudflare" {
  api_token = var.cloudflare_api_token
}

data "google_project" "project" {}

module "vpc" {
  source = "../../modules/vpc"

  region                  = var.region
  network_name            = "morupark-vpc-prod"
  subnet_name             = "morupark-subnet-prod"
  subnet_cidr             = "10.0.1.0/24"
  allow_lb_firewall_name  = "allow-lb-ingress-prod"
  allow_gke_firewall_name = "allow-gke-traffic-prod"
  allow_ssh_firewall_name = "allow-ssh-iap-prod"
}

module "gke" {
  source = "../../modules/gke"

  project_id          = var.project_id
  region              = var.region
  cluster_name        = "morupark-gke-prod"
  network_id          = module.vpc.network_id
  subnetwork_id       = module.vpc.subnetwork_id
  authorized_ip_cidrs = var.authorized_ip_cidrs
}

module "db" {
  source = "../../modules/db"

  region                = var.region
  zone                  = var.zone
  instance_name         = "morupark-db-prod"
  database_name         = "moruparkdb"
  db_username           = var.db_username
  db_password           = var.db_password
  admin_authorized_cidr = "175.198.119.106/32"
}
