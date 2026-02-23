############################################
# Common locals
############################################
locals {
  project_id = data.google_project.project.project_id

  services = {
    auth  = "auth-service-sa"
    queue = "queue-service-sa"
  }
}

############################################
# Google Service Accounts (GSA)
############################################
resource "google_service_account" "gsa" {
  for_each = local.services

  account_id   = each.value
  display_name = "${each.key} service GSA"
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
