terraform {
  required_version = ">= 1.0.0"

  backend "gcs" {
    bucket = "morupark-tfstate-bucket"
    prefix = "terraform/state/public"
  }

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
  zone    = var.zone
}

data "google_project" "project" {}