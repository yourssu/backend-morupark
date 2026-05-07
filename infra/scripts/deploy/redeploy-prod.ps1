param(
  [string]$PlanFile = "prod.tfplan",
  [switch]$SkipDbCreate,
  [switch]$SkipPreflight,
  [switch]$AllowDestroyPlan
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$tfDir = Join-Path $repoRoot "terraform/environments/prod"
$dbScript = Join-Path $repoRoot "scripts/create_moruparkdb_prod.py"
$overlayDir = Join-Path $repoRoot "k8s/overlays/prod"
$expectedProjectId = "yourssu-morupark-494902"

function Assert-CommandSucceeded {
  param([string]$Step)
  if ($LASTEXITCODE -ne 0) {
    throw "$Step failed with exit code $LASTEXITCODE"
  }
}

function Run-Preflight {
  Write-Host "[] Preflight: terraform show -json sanity check" -ForegroundColor Cyan
  $planJson = terraform -chdir="$tfDir" show -json $PlanFile | ConvertFrom-Json
  Assert-CommandSucceeded "terraform show -json"

  if ($planJson.resource_changes) {
    $destroyActions = @($planJson.resource_changes | Where-Object {
        $_.change.actions -contains "delete"
      })
    if ($destroyActions.Count -gt 0 -and -not $AllowDestroyPlan) {
      throw "Preflight failed: plan contains delete actions. Refusing to run apply via redeploy script."
    } elseif ($destroyActions.Count -gt 0 -and $AllowDestroyPlan) {
      Write-Host "[] Warning: destroy actions detected, but -AllowDestroyPlan is set." -ForegroundColor Yellow
    }
  }

  $dbChanges = @($planJson.resource_changes | Where-Object {
      $_.type -eq "google_sql_database_instance" -and (
        $_.change.actions -contains "delete" -or $_.change.actions -contains "replace"
      )
    })
  if ($dbChanges.Count -gt 0) {
    throw "Preflight failed: Cloud SQL instance has delete/replace actions in plan."
  }

  Write-Host "[] Preflight: kubectl kustomize manifest guardrail" -ForegroundColor Cyan
  $rendered = kubectl kustomize "$overlayDir"
  Assert-CommandSucceeded "kubectl kustomize"

  if ($rendered -match "docker\.pkg\.dev/yourssu-morupark/morupark-repo-private/") {
    throw "Preflight failed: found legacy image project_id path yourssu-morupark."
  }
  if ($rendered -match "iam\.gke\.io/gcp-service-account:\s*[^\s]+@yourssu-morupark\.iam\.gserviceaccount\.com") {
    throw "Preflight failed: found legacy Workload Identity annotation project_id."
  }

  Write-Host "[] Preflight: WI mapping consistency check" -ForegroundColor Cyan
  $requiredMappings = @(
    "auth-service-sa-prod@${expectedProjectId}.iam.gserviceaccount.com",
    "queue-service-sa-prod@${expectedProjectId}.iam.gserviceaccount.com",
    "goods-service-sa-prod@${expectedProjectId}.iam.gserviceaccount.com"
  )
  foreach ($mapping in $requiredMappings) {
    if ($rendered -notmatch [regex]::Escape($mapping)) {
      throw "Preflight failed: missing required WI mapping -> $mapping"
    }
  }
}

if (-not $SkipPreflight) {
  Run-Preflight
} else {
  Write-Host "[] Skip preflight step" -ForegroundColor Yellow
}

Write-Host "[] Terraform apply start" -ForegroundColor Cyan
terraform -chdir="$tfDir" apply $PlanFile
Assert-CommandSucceeded "terraform apply"

if (-not $SkipDbCreate) {
  Write-Host "[] Ensuring Cloud SQL DB moruparkdb_prod" -ForegroundColor Cyan
  python "$dbScript"
  Assert-CommandSucceeded "python create_moruparkdb_prod.py"
} else {
  Write-Host "[] Skip DB create step" -ForegroundColor Yellow
}

Write-Host "[] Done" -ForegroundColor Green
