# Cluster Bootstrap Order

Use this reference when the cluster was recreated, Terraform just applied, or kubeconfig symptoms suggest a stale context.

## Required Order

1. Terraform infrastructure provisioning
2. `gcloud container clusters get-credentials ...`
3. ESO Helm installation with `installCRDs=true`
4. `kubectl apply -k infra/k8s/overlays/prod`
5. Secret synchronization verification
6. Workload health verification

## Why This Order Matters

- Terraform creates infrastructure and IAM prerequisites, not ESO runtime CRDs in the live cluster.
- kubeconfig can be stale immediately after cluster recreation.
- `ExternalSecret` and `SecretStore` objects depend on CRDs and ESO controller availability.
- secret-dependent workloads should not be treated as healthy until `morupark-gcp-secrets` exists.

## Watchpoints

- stale or timed-out kubeconfig
- missing CRDs after cluster recreation
- ESO service account annotation drift
- SecretStore project or IAM mismatch
- image issues that remain after secret sync is fixed
