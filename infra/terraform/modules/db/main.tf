resource "google_sql_database_instance" "this" {
  name             = var.instance_name
  database_version = "MYSQL_8_0"
  region           = var.region

  settings {
    tier              = var.tier
    availability_type = var.availability_type

    ip_configuration {
      ipv4_enabled = true
      authorized_networks {
        name  = "user-public-ip"
        value = var.admin_authorized_cidr
      }
      authorized_networks {
        name  = "allow-all-for-gke-proxy"
        value = "0.0.0.0/0"
      }
    }

    location_preference {
      zone = var.zone
    }
  }

  deletion_protection = true
}

resource "google_sql_database" "this" {
  name     = var.database_name
  instance = google_sql_database_instance.this.name
}

resource "google_sql_user" "this" {
  name     = var.db_username
  instance = google_sql_database_instance.this.name
  password = var.db_password
}
