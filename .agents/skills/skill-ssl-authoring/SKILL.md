---
name: skill-ssl-authoring
description: Create or update repo-local Codex skills using a split between concise human-readable `SKILL.md` guidance and machine-readable `ssl.json` evidence graphs. Use when Codex needs to author new skills, convert existing text-first skills such as `.gemini/skills/*.md`, or standardize future skills around Scheduling, Structural, and Logical SSL layers with grounded resource and risk metadata.
---

# Skill SSL Authoring

Use this skill to create or update repo-local skills under `.agents/skills/` with a sidecar `ssl.json`.

## Output Contract

Produce these files for each managed skill:

1. `SKILL.md`
2. `ssl.json`
3. `agents/openai.yaml`
4. only the `references/`, `scripts/`, or `assets/` resources the skill actually needs

## Authoring Rules

1. Keep `SKILL.md` short, procedural, and operator-facing.
2. Put invocation fit, scene flow, and atomic action/resource evidence in `ssl.json`.
3. Ground every SSL field in repo evidence or the source skill text.
4. Prefer a small enum set for logical steps: `READ`, `WRITE`, `VALIDATE`, `SELECT`, `INFER`, `EXECUTE`, `VERIFY`.
5. Add references when the details would otherwise bloat `SKILL.md`.

## Conversion Flow

1. Read the source workflow or skill text.
2. Extract invocation clues into the scheduling layer.
3. Decompose the workflow into scenes for the structural layer.
4. Break each scene into atomic actions for the logical layer.
5. Record evidence sources in `grounding`.
6. Validate that transitions, identifiers, and resource targets are internally consistent.

## References

- Schema guide: [references/ssl-schema-guide.md](references/ssl-schema-guide.md)
- Conversion playbook: [references/conversion-playbook.md](references/conversion-playbook.md)
- JSON starter: [assets/ssl-template.json](assets/ssl-template.json)
