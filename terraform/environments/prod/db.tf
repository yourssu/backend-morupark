resource "google_sql_database_instance" "mysql_instance" {
  name             = "morupark-db-prod"
  database_version = "MYSQL_8_0"
  region           = var.region

  settings {
    tier = "db-g1-small"

    # 비용 최적화를 위해 ZONAL(단일 영역) 사용 (사용자 요청: 가장 낮은 스펙 우선)
    availability_type = "ZONAL"

    ip_configuration {
      ipv4_enabled = true
      authorized_networks {
        name  = "user-public-ip"
        value = "175.198.119.106/32"
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
  deletion_protection = false
}

# 파드들이 공유하는 DB
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
