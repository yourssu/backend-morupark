# Operating Order

Use this reference when the request touches production infra or cluster restoration.

## Standard Order

1. Identify the request class.
2. Choose the matching skill.
3. Validate environment and guardrails.
4. Inspect before mutating.
5. Rebuild order when needed:
   - Terraform
   - kubeconfig refresh
   - ESO bootstrap
   - Kustomize apply
   - post-deploy verification
6. End with explicit verification and the smallest next safe action.

## Why This Matters

- repeated incidents in this repo often come from doing the right steps in the wrong order
- ESO bootstrap and workload rollout must remain separate
- post-deploy verification catches image, secret, and probe failures early
