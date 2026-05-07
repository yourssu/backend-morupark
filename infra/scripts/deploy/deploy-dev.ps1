param(
  [ValidateSet("all", "auth", "queue", "goods", "api-gateway")]
  [string]$Service = "all",
  [switch]$SkipTests,
  [switch]$RestartDeployment
)

$ErrorActionPreference = "Stop"

$ProjectId = "yourssu-morupark-494902"
$Region = "asia-northeast3"
$Repository = "morupark-repo-private"
$Registry = "$Region-docker.pkg.dev"

$services = @(
  @{ Name = "auth"; Module = "services-auth"; Image = "auth-service"; Deployment = "auth-service" },
  @{ Name = "queue"; Module = "services-queue"; Image = "queue-service"; Deployment = "queue-service" },
  @{ Name = "goods"; Module = "services-goods"; Image = "goods-service"; Deployment = "goods-service" },
  @{ Name = "api-gateway"; Module = "services-api-gateway"; Image = "api-gateway"; Deployment = "api-gateway" }
)

if ($Service -eq "all") {
  $targets = $services
} else {
  $targets = $services | Where-Object { $_.Name -eq $Service }
}

if (-not $targets -or $targets.Count -eq 0) {
  throw "No target services matched: $Service"
}

Write-Host "Using project: $ProjectId" -ForegroundColor Cyan
gcloud config set project $ProjectId | Out-Null
gcloud auth configure-docker $Registry --quiet | Out-Null

$sha = (git rev-parse --short=8 HEAD).Trim()
$tag = "sha-$sha"

foreach ($svc in $targets) {
  Write-Host "=== Processing $($svc.Name) ===" -ForegroundColor Green

  if (-not $SkipTests) {
    Write-Host "Running Gradle tests/build for $($svc.Module)" -ForegroundColor Yellow
    ./gradlew ":$($svc.Module):clean" ":$($svc.Module):build" --refresh-dependencies
  } else {
    Write-Host "Skipping tests for $($svc.Module)" -ForegroundColor Yellow
    ./gradlew ":$($svc.Module):clean" ":$($svc.Module):build" -x test --refresh-dependencies
  }

  $imageBase = "$Registry/$ProjectId/$Repository/$($svc.Image)"
  $tagSha = "${imageBase}:$tag"
  $tagLatest = "${imageBase}:latest"

  Write-Host "Building image: $tagSha" -ForegroundColor Yellow
  docker build --platform linux/amd64 -t $tagSha -f "$($svc.Module)/Dockerfile" .

  Write-Host "Pushing image: $tagSha" -ForegroundColor Yellow
  docker push $tagSha

  Write-Host "Tagging/pushing latest: $tagLatest" -ForegroundColor Yellow
  docker tag $tagSha $tagLatest
  docker push $tagLatest

  if ($RestartDeployment) {
    Write-Host "Restart deployment: $($svc.Deployment)" -ForegroundColor Yellow
    kubectl rollout restart deployment $($svc.Deployment) -n morupark-prod
  }
}

Write-Host "Done. Pushed images to Artifact Registry." -ForegroundColor Green
Write-Host "Tag used: $tag" -ForegroundColor Cyan
Write-Host "If ArgoCD Image Updater is active, deployment should follow automatically." -ForegroundColor Cyan