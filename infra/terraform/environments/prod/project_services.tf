resource "google_project_service" "required" {
  for_each = toset([
    "compute.googleapis.com",
    "container.googleapis.com",
    "secretmanager.googleapis.com",
    "sqladmin.googleapis.com",
    "iam.googleapis.com",
    "servicenetworking.googleapis.com"
  ])

  project = var.project_id
  service = each.value

  disable_on_destroy = false
}
