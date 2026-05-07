---
name: infra-ops
description: Repo-local infrastructure operations workflow for this MoruPark repository. Use when Codex needs to inspect, plan, review, or execute repeated work under `infra/`, especially Terraform provisioning in `infra/terraform/environments/prod`, preflight checks, post-apply verification, Cloud SQL follow-up tasks, and handoff checks tied to GCP, GKE, Secret Manager, Cloudflare, or Kubernetes overlays.
---

# Infra Ops

Use this skill for repo-local `/infra` work. Keep `SKILL.md` concise for operator guidance and use `ssl.json` as the structured invocation, scene, and resource graph.

## Quick Start

1. Start from [infra/README.md](infra/README.md) and the target environment under `infra/terraform/environments/prod`.
2. Read `ssl.json` when the request needs machine-readable invocation fit, execution scenes, or resource-risk evidence.
3. Read [references/terraform-workflow.md](references/terraform-workflow.md) for the Terraform path, variables, and guardrails.
4. Read [references/ops-followups.md](references/ops-followups.md) when the task includes DB creation, K8s handoff, or post-apply verification.

## Workflow

1. Run a preflight pass before any mutable action.
2. Confirm the exact environment, path, and user intent before planning changes.
3. Inspect variables, credentials, and provider-sensitive inputs before `plan`.
4. Prefer `fmt -check`, `validate`, and `plan -out=prod.tfplan` before any `apply`.
5. Treat `apply` as approval-gated and never default to `-auto-approve`.
6. Verify outputs and downstream state after provisioning.
7. Use repo utility scripts only when the requested follow-up is in scope.

## Guardrails

- Do not mutate infra outside the intended environment path.
- Do not skip `plan` when a mutable Terraform action is requested.
- Do not use `terraform apply -auto-approve`.
- Do not assume optional secrets or Cloudflare inputs are populated without checking.
- Do not create or alter production resources unless the user request clearly calls for it.

## References

- Terraform flow: [references/terraform-workflow.md](references/terraform-workflow.md)
- Follow-up operations: [references/ops-followups.md](references/ops-followups.md)
- Structured graph: [ssl.json](ssl.json)
