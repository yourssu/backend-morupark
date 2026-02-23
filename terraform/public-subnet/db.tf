resource "google_sql_database_instance" "mysql_instance" {
  name             = "morupark-db-public"
  database_version = "MYSQL_8_0"
  region           = "asia-northeast3"

  settings {
    tier = "db-f1-micro" # 최소 사양
    ip_configuration {
      ipv4_enabled = true
      authorized_networks {
        name  = "user-public-ip"
        value = "175.198.119.106/32"
      }
      authorized_networks {
        name  = "allow-all-for-gke-proxy"
        value = "0.0.0.0/0"
        # NOTE: This allows all IPs. It's safe when using the Cloud SQL Auth Proxy
        # but should be restricted to specific GKE node IPs or a NAT Gateway IP in production.
      }
    }
    location_preference {
      zone = "asia-northeast3-a"
    }
  }
  deletion_protection = false
}

# 모든 파드들이 공유하는 통합 DB 생성
resource "google_sql_database" "morupark_db" {
  name     = "moruparkdb"
  instance = google_sql_database_instance.mysql_instance.name
}

data "google_secret_manager_secret_version" "db_password" {
  secret  = "morupark-db-password"
  version = "latest"
}

resource "google_sql_user" "db_user" {
  name     = var.db_username
  instance = google_sql_database_instance.mysql_instance.name
  password = data.google_secret_manager_secret_version.db_password.secret_data
}