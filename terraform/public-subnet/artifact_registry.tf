resource "google_artifact_registry_repository" "morupark_repo" {
  location      = "asia-northeast3"
  repository_id = "morupark-repo"
  description   = "Docker repository for morupark services"
  format        = "DOCKER"

  docker_config {
    immutable_tags = false
  }
}