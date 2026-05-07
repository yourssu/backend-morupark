# ESO Root Cause And Recovery

Use this reference when `ExternalSecret`, `SecretStore`, or synced Secret failures appear in prod.

## Repeated Failure Pattern

The recurring incident shape in this repo is:

1. Terraform or cluster reprovisioning completes.
2. ESO or its CRDs are not yet installed in the cluster.
3. `kubectl apply -k infra/k8s/overlays/prod` runs too early.
4. `ExternalSecret` and `SecretStore` resources fail because the CRDs are missing.
5. `morupark-gcp-secrets` is not created.
6. Dependent Pods fail to start or stall in secret-related error states.

## Typical Symptoms

- `no matches for kind "ExternalSecret" in version "external-secrets.io/v1"`
- `no matches for kind "SecretStore" in version "external-secrets.io/v1"`
- missing `morupark-gcp-secrets`
- downstream `CreateContainerConfigError`

## Standard Recovery

1. Ensure the Kubernetes context points at the intended GKE cluster.
2. Install or recover ESO with:
   - `helm upgrade --install external-secrets external-secrets/external-secrets -n external-secrets --create-namespace --set installCRDs=true ...`
3. Verify:
   - `kubectl get crd externalsecrets.external-secrets.io secretstores.external-secrets.io`
4. Re-apply:
   - `kubectl apply -k infra/k8s/overlays/prod`
5. Verify:
   - `kubectl get externalsecret -n morupark-prod`
   - `kubectl get secret morupark-gcp-secrets -n morupark-prod`
   - `kubectl get pods -n morupark-prod`

## Prevention Gates

- Gate overlay apply on ESO CRD existence.
- Gate workload diagnosis on synced Secret presence.
- Treat cluster rebuilds as requiring a fresh ESO bootstrap check.
