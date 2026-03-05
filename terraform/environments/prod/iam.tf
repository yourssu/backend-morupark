############################################
# Common locals
############################################
locals {
  project_id = data.google_project.project.project_id

  services = {
    auth  = "auth-service-sa-prod"
    queue = "queue-service-sa-prod"
    goods = "goods-service-sa-prod"
  }
}

############################################
# Google Service Accounts (GSA)
############################################
resource "google_service_account" "gsa" {
  for_each = local.services

  account_id   = each.value
  display_name = "${each.key} service GSA (Prod)"
}

############################################
# Allow KSA -> GSA impersonation (Workload Identity)
############################################
resource "google_service_account_iam_member" "workload_identity" {
  for_each = local.services

  service_account_id = google_service_account.gsa[each.key].name
  role               = "roles/iam.workloadIdentityUser"

  member = "serviceAccount:${local.project_id}.svc.id.goog[${var.namespace}/${each.value}]"
}

############################################
# Cloud SQL 권한
############################################
resource "google_project_iam_member" "cloudsql_client" {
  for_each = local.services

  project = local.project_id
  role    = "roles/cloudsql.client"
  member  = google_service_account.gsa[each.key].member
}

############################################
# Kafka 권한 (Managed Kafka 접근용) - 삭제 / 주석 처리함
############################################
/*
resource "google_project_iam_member" "kafka_client" {
  for_each = local.services
  project  = local.project_id
  # Managed Kafka Client 권한 부여
  role   = "roles/managedkafka.client"
  member = google_service_account.gsa[each.key].member
}
*/

############################################
# GKE Node Artifact Registry Access
############################################
resource "google_project_iam_member" "gke_node_ar_reader" {
  project = local.project_id
  role    = "roles/artifactregistry.reader"
  member  = "serviceAccount:${data.google_project.project.number}-compute@developer.gserviceaccount.com"
}
