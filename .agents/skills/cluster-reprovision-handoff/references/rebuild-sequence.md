# Rebuild Sequence

Use this reference after `terraform destroy`, fresh cluster creation, or major infra reprovisioning.

## Canonical Order

1. Review the Terraform target and intended prod environment.
2. Run the Terraform validation and plan path first.
3. Apply Terraform only with explicit approval.
4. Refresh kubeconfig with `gcloud container clusters get-credentials ...`.
5. Install or verify ESO before applying app overlays.
6. Apply `infra/k8s/overlays/prod`.
7. Verify secrets, workloads, ingress, and remaining blockers.

## Why This Split Exists

- Terraform provisions cloud resources and IAM, not all cluster runtime operators.
- kubeconfig often becomes stale after cluster recreation.
- app overlays contain resources that depend on operators and CRDs.
- downstream failures can shift from infra readiness to image or app issues after the secret layer is fixed.
