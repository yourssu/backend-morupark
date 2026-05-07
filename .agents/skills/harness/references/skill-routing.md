# Skill Routing

Use this reference to choose the right operations skill quickly.

## If the issue is about secrets or ESO

Choose `eso-recurrence-prevention` when:

- `ExternalSecret` or `SecretStore` fails
- CRDs are missing
- `morupark-gcp-secrets` is missing
- cluster recreation caused secret sync regression

## If the issue is about rebuild or reprovisioning

Choose `cluster-reprovision-handoff` when:

- Terraform just recreated infra
- kubeconfig must be refreshed
- ESO must be bootstrapped again
- app overlays must be re-applied after rebuild

## If the issue is about rollout or release state

Choose `k8s-release-verification` when:

- rollout health must be checked
- `ImagePullBackOff` or probe errors appear
- overlay, namespace, HPA, or StatefulSet expectations must be verified

## If the issue is broader Terraform infra work

Choose `infra-ops` when:

- the main task is Terraform planning or apply review
- GCP resources, IAM, DNS, or outputs are the main focus
