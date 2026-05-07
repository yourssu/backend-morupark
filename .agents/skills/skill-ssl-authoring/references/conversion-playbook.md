# Conversion Playbook

Use this playbook to convert text-first skills into `SKILL.md + ssl.json`.

## Source Candidates

Typical source material in this repo:

- `.gemini/skills/*.md`
- repo workflow notes
- bootstrap scripts
- infra or service-specific runbooks

## Step 1: Extract Scheduling Signals

Look for:

- what requests should trigger the skill
- what environment or path it applies to
- required inputs and expected outputs
- obvious risk or mutation markers
- named dependencies such as files, tools, or providers

Write these into `scheduling`.

## Step 2: Decompose the Workflow into Scenes

Turn the prose workflow into a small scene graph.

Good scene patterns:

- `preflight`
- `inspect-context`
- `validate-inputs`
- `plan`
- `execute`
- `verify`
- `recovery`

For each scene, define:

- purpose
- inputs
- outputs
- entry and exit conditions
- next-scene rules

## Step 3: Extract Atomic Actions

For each scene, identify the smallest meaningful actions.

Examples:

- read a file
- validate a variable set
- execute a CLI command
- verify a generated artifact
- write a new skill file

Map each action to:

- `act_type`
- `resource_scope`
- `resource_target`
- `success_next`
- `failure_next`

## Step 4: Write the Human-Facing Skill

Keep `SKILL.md` short.

It should contain:

- what the skill is for
- a quick start
- the main workflow
- guardrails
- links to references and `ssl.json`

Do not duplicate the full graph in prose.

## Step 5: Add Grounding

For each high-impact claim, add a concrete source path.

Prefer this format:

- exact repo path
- one-line evidence summary

## Extraction Checklist

- invocation clues captured
- environment or target path captured
- mutation and risk flags captured
- scene graph has a clear entry scene
- every scene has transitions
- logical steps resolve to real scenes
- resource targets are specific enough to review
- grounding points to local source evidence
