# Post Provision Checks

Use this reference after the rebuild path reaches Kubernetes verification.

## Minimum Checks

- `kubectl get deploy -n morupark-prod`
- `kubectl get statefulset -n morupark-prod`
- `kubectl get hpa -n morupark-prod`
- `kubectl get pods -n morupark-prod -o wide`

## Additional Checks

- `kubectl get secret morupark-gcp-secrets -n morupark-prod`
- `kubectl get externalsecret -n morupark-prod`
- ingress static IP and certificate readiness
- image pull failures or wrong tag propagation

## Common Follow-Up Split

- If secrets are missing, return to ESO/bootstrap checks.
- If secrets are healthy but pods fail, inspect image, probes, config, or app startup.
- If ingress is missing, inspect Terraform outputs and overlay ingress wiring.
