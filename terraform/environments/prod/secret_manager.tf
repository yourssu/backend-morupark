resource "google_secret_manager_secret" "jwt_secret" {
  secret_id = "morupark-jwt-secret-prod"
  replication {
    auto {}
  }
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
