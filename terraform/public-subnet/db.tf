resource "google_sql_database_instance" "mysql_instance" {
  name             = "morupark-db-public"
  database_version = "MYSQL_8_0"
  region           = "asia-northeast3"

  settings {
    tier = "db-f1-micro" # 최소 사양
    ip_configuration {
      ipv4_enabled = true
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