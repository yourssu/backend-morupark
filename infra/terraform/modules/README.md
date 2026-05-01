# Terraform Modules

This folder contains reusable Terraform modules (for example `vpc`, `gke`, `db`).

`infra/terraform/environments/*` should call these modules to avoid duplication
between environments such as `dev` and `prod`.

Current status:
- Module directories are scaffolded.
- Existing `prod` resources remain in-place and can be migrated incrementally.
