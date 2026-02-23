resource "google_secret_manager_secret" "jwt_secret" {
  secret_id = "morupark-jwt-secret"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "admin_key" {
  secret_id = "morupark-admin-key"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_iam_member" "gke_secret_access" {
  project   = data.google_project.project.project_id
  secret_id = "morupark-db-password"
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${var.project_id}.svc.id.goog[morupark/morupark-sa]"

  # GKE 클러스터가 먼저 생성되어야 Identity Pool이 유효함
  depends_on = [google_container_cluster.morupark_gke]
}