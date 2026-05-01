resource "google_secret_manager_secret" "jwt_secret" {
  secret_id = "morupark-jwt-secret-prod"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "jwt_secret_version" {
  secret      = google_secret_manager_secret.jwt_secret.id
  secret_data = "temporary-jwt-secret-replace-me-in-production"
}

# 기존에 존재하는 시크릿을 data 소스로 참조
data "google_secret_manager_secret" "db_username_secret" {
  secret_id = "morupark-db-username"
}

resource "google_secret_manager_secret_version" "db_username_version" {
  secret      = data.google_secret_manager_secret.db_username_secret.id
  secret_data = var.db_username
}

data "google_secret_manager_secret" "db_password_secret" {
  secret_id = "morupark-db-password"
}

data "google_secret_manager_secret_version" "db_password_version" {
  secret = data.google_secret_manager_secret.db_password_secret.id
}

data "google_secret_manager_secret" "redis_password_secret" {
  secret_id = "morupark-redis-password"
}

resource "google_secret_manager_secret_version" "redis_password_version" {
  secret      = data.google_secret_manager_secret.redis_password_secret.id
  secret_data = var.db_password
}

resource "google_secret_manager_secret" "admin_key" {
  secret_id = "morupark-admin-key-prod"
  replication {
    auto {}
  }
}

resource "google_service_account" "eso_sa" {
  account_id   = "eso-sa-prod"
  display_name = "External Secrets Operator GSA"
}

resource "google_project_iam_member" "eso_secret_accessor" {
  project = data.google_project.project.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${google_service_account.eso_sa.email}"
}

resource "google_service_account_iam_member" "eso_workload_identity" {
  service_account_id = google_service_account.eso_sa.name
  role               = "roles/iam.workloadIdentityUser"
  member             = "serviceAccount:${data.google_project.project.project_id}.svc.id.goog[external-secrets/external-secrets]"
}
