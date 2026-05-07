# Common Release Failures

Use this reference when the rollout is unhealthy.

## Secret Class

- missing `morupark-gcp-secrets`
- `ExternalSecret` not ready
- `CreateContainerConfigError`

## Image Class

- `ImagePullBackOff`
- wrong `latest` or `sha-*` tag propagation
- pushed image missing in Artifact Registry

## Probe Class

- readiness or liveness endpoint mismatch
- actuator path drift
- container starts but never becomes Ready

## Config Class

- wrong namespace
- wrong ServiceAccount name
- prod overlay drift

## StatefulSet Class

- Redis checked like a Deployment instead of StatefulSet
- PVC or config changes not reflected in expectations
