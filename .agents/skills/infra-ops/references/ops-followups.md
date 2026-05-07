# Ops Follow-Ups

Use this reference after Terraform review or provisioning when the task extends into downstream infra operations.

## Database Creation

Grounded source:

- `infra/scripts/utility/create_moruparkdb_prod.py`

The script creates:

- instance: `morupark-db-prod`
- database: `moruparkdb_prod`
- project: `yourssu-morupark-494902`

Use this only when the request explicitly includes DB setup or post-provision DB creation. Treat it as a cloud mutation step.

## Secret and Identity Handoffs

Read `infra/terraform/environments/prod/secret_manager.tf` and `iam.tf` when follow-up checks involve:

- Secret Manager secret presence
- External Secrets Operator service account wiring
- workload identity bindings
- service account IAM coverage for auth, goods, and queue services

## DNS and Ingress Handoffs

Read:

- `infra/terraform/environments/prod/outputs.tf`
- `infra/terraform/environments/prod/dns.tf`
- `infra/terraform/environments/prod/ingress.tf`

Check whether:

- the static IP output changed
- Cloudflare DNS management is active
- a manual DNS handoff is still needed

## Kubernetes Overlay Follow-Ups

When Terraform changes infrastructure consumed by Kubernetes, inspect:

- `infra/k8s/base`
- `infra/k8s/overlays/prod`

Typical follow-up questions:

- Does the namespace or overlay expect a new secret, IP, or service account?
- Does prod ingress need to match the Terraform-managed static IP or certificate?
- Do External Secret resources depend on newly created Secret Manager assets?

## Safe Completion Pattern

Prefer this completion pattern:

1. Summarize what Terraform changed or would change.
2. List downstream resources that may need attention.
3. Execute follow-up mutations only when they were requested.
4. End with the smallest safe next action if any prerequisite is missing.
