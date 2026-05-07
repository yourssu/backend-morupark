# SSL Schema Guide

Use this reference when creating repo-local skills with a sidecar `ssl.json`.

## File Split

Each managed skill should keep responsibilities separate:

- `SKILL.md`
  - concise, human-readable guidance
  - what the operator or agent should do at a high level
- `ssl.json`
  - machine-readable invocation, execution, and resource graph
- `agents/openai.yaml`
  - UI-facing metadata for discovery

## Required Top-Level Keys

Use these top-level keys in `ssl.json`:

- `skill`
- `scheduling`
- `structural`
- `logical`
- `grounding`

## Recommended Shape

### `skill`

- `name`
- `kind`
- `sidecar_format`
- `human_document`

### `scheduling`

- `goal`
- `trigger_phrases`
- `intent_signatures`
- `expected_inputs`
- `expected_outputs`
- `tags`
- `dependencies`
- `control_signals`
- `mutation_profile`
- `sensitive_resource_flags`

### `structural`

- `entry_scene`
- `scenes`

Each scene should have:

- `id`
- `purpose`
- `inputs`
- `outputs`
- `entry_conditions`
- `exit_conditions`
- `next_scene_rules`

### `logical`

- `entry_step`
- `steps`

Each step should have:

- `id`
- `scene_id`
- `act_type`
- `resource_scope`
- `resource_target`
- `success_next`
- `failure_next`

### `grounding`

- `policy`
- `sources`

Each source should have:

- `path`
- `evidence`

## Allowed Logical Enums

Start with this repo-local enum set:

- `READ`
- `WRITE`
- `VALIDATE`
- `SELECT`
- `INFER`
- `EXECUTE`
- `VERIFY`

Do not add new enums unless the current skill cannot be represented cleanly without them.

## Grounding Rules

1. Extract only what the source documents support.
2. Prefer repo-local paths over free-form summaries.
3. If a transition or resource cannot be grounded, simplify the graph instead of inventing detail.
4. Treat `ssl.json` as an evidence view, not a speculative rewrite.

## Validation Expectations

Review these checks before finalizing:

- every `scene_id` in `logical.steps` exists in `structural.scenes`
- every `goto`, `success_next`, and `failure_next` target resolves
- `entry_scene` and `entry_step` exist
- sensitive resource flags match the actual resources mentioned
- grounding sources cover the high-risk or high-impact parts of the graph
