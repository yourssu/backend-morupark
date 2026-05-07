# Terraform Workflow

Use this reference when the request touches Terraform under `infra/terraform/environments/prod`.

## Repo Topology

- Environment root: `infra/terraform/environments/prod`
- Reusable modules:
  - `infra/terraform/modules/vpc`
  - `infra/terraform/modules/gke`
  - `infra/terraform/modules/db`
- Existing text skill source:
  - `.gemini/skills/terraform-prod-ops.md`

## Grounded Inputs

Read `variables.tf` and confirm which of these matter for the current request:

- `project_id`
- `region`
- `zone`
- `db_username`
- `db_password`
- `admin_key`
- `authorized_ip_cidrs`
- `domain`
- `cloudflare_api_token`
- `cloudflare_zone_id`
- `cloudflare_proxied`
- `namespace`
- `GOOGLE_CREDENTIALS`

Treat these as sensitive or mutation-relevant:

- `db_password`
- `admin_key`
- `cloudflare_api_token`
- `cloudflare_zone_id`
- `GOOGLE_CREDENTIALS`

## Provisioning Topology

`main.tf` wires these major resources:

- `module.vpc`
  - network and subnet resources for prod
- `module.gke`
  - GKE cluster using the VPC and subnetwork
- `module.db`
  - Cloud SQL instance and base database resource

Additional environment resources include:

- `project_services.tf`
  - enables required Google APIs
- `secret_manager.tf`
  - JWT, DB, Redis, admin key secrets and ESO service account wiring
- `dns.tf`
  - conditional Cloudflare DNS record creation
- `ingress.tf`
  - managed certificate and static IP resources
- `iam.tf`
  - service account and IAM wiring
- `moved.tf`
  - migration mapping from older resource addresses to module-backed addresses

## Expected Order

Use this order unless the user asks for a narrower read-only operation:

1. Inspect the target path and read the current environment files.
2. Confirm required tools and expected paths from `infra/scripts/setup/setup-harness.sh`.
3. Check variable-sensitive inputs before Terraform execution.
4. Run:
   - `terraform -chdir=infra/terraform/environments/prod fmt -check`
   - `terraform -chdir=infra/terraform/environments/prod validate`
   - `terraform -chdir=infra/terraform/environments/prod plan -out=prod.tfplan`
5. Only after explicit mutation intent, run:
   - `terraform -chdir=infra/terraform/environments/prod apply prod.tfplan`
6. Verify outputs and downstream handoff needs.

## Guardrails

- Never default to `terraform apply -auto-approve`.
- Do not mutate from a path outside `infra/terraform/environments/prod` when the task is prod-scoped.
- Prefer a plan artifact before apply.
- Do not assume optional Cloudflare inputs are present.
- Treat `secret_manager.tf` values and provider credentials as sensitive.
- Read `moved.tf` before drawing conclusions about renamed or relocated resources.

## Verification Targets

After a successful plan or apply, inspect at least the relevant outputs and downstream state:

- `outputs.tf`
  - ingress static IP for DNS handoff
- `dns.tf`
  - whether Cloudflare record creation is active
- `secret_manager.tf`
  - whether new resources imply secret distribution follow-up
- `infra/k8s/overlays/prod`
  - when Kubernetes manifests depend on the provisioned infrastructure
