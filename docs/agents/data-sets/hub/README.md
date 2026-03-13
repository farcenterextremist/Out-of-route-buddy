# Hub — Shared completed polished data sets

**Purpose:** Single place for **all agents** (roles) to deposit their **completed, polished** data sets so others can consume them from one hub. No scattering across role-specific folders; finished work lives here.

**Path:** `docs/agents/data-sets/hub/`  
**References:** [DATA_SETS_AND_DELEGATION_PLAN.md](../../DATA_SETS_AND_DELEGATION_PLAN.md), [data-sets README](../README.md)

**→ All agents running any loop:** Read [UNIVERSAL_LOOP_PROMPT.md](./UNIVERSAL_LOOP_PROMPT.md). It ensures every loop run consults this Hub via the Loop Master and minimizes AI slop.

---

## How to use (agents)

| Step | Action |
|------|--------|
| **When you finish** | When your role has produced a **completed, polished** data set (report, export, brief, index, artifact), place it here. |
| **Naming** | Use a clear name: `YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>`. Example: `2025-03-12_file-organizer_doc-index.md`. |
| **Index** | Optionally add a one-line entry to the Hub index in this file (table below). |
| **Send to hub (user says)** | When the user says **"send to hub"**, save the precious data (your completed, polished output) to **this folder**. Name files: `YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>`. Optionally add a one-line entry to this README. Hub = this data folder. Not GitHub. See [SEND_TO_HUB_PROMPT.md](./SEND_TO_HUB_PROMPT.md). |

**Rule:** Only **polished, completed** outputs. Work-in-progress stays in role-specific data sets or working docs.

---

## Hub index (optional)

*(Add rows when you deposit a file. Keeps the hub discoverable.)*

| Date | File | Role / topic | One-line description |
|------|------|--------------|----------------------|
| 2026-03-11 | [2026-03-11_file-organizer_data-sets-index-and-organization.md](./2026-03-11_file-organizer_data-sets-index-and-organization.md) | file-organizer | Data-sets index and organization: hub, role data sets, aptitude, security, board-meeting, quick rules. |
| 2026-03-11 | [2026-03-11_token-loop_loop5-report-proof-of-work-and-benefits.md](./2026-03-11_token-loop_loop5-report-proof-of-work-and-benefits.md) | token-loop | Loop #5: proof of work, benefits, rule output, next TODOs; §4.4 now requires Loop # + proof + benefits every run. |
| 2026-03-11 | [2026-03-11_data-organized_token-and-automation-index.md](./2026-03-11_data-organized_token-and-automation-index.md) | data-organized | Index: where token/automation data lives, current state (loop count, rule metrics), how to use (next loop, other agents). |
| 2026-03-11 | [2026-03-11_cyber-security_loop3-proof-of-work-and-benefits.md](./2026-03-11_cyber-security_loop3-proof-of-work-and-benefits.md) | cyber-security | Loop #3: proof of work, 4/4 validation passed, benefits, how to utilize. |
| 2026-03-11 | [2026-03-11_cyber-security_purple-training.json](./2026-03-11_cyber-security_purple-training.json) | cyber-security | Structured training data: validation_simulations, synthetic_scenarios, summary for Red/Blue agents. |
| 2026-03-11 | [2026-03-11_cyber-security_data-summary-and-utilization.md](./2026-03-11_cyber-security_data-summary-and-utilization.md) | cyber-security | Data collected, what it means, how to use (regression, few-shot, metrics, rollback). |
| 2026-03-12 | [2026-03-12_codey_code-structure-and-compiler-brief.md](./2026-03-12_codey_code-structure-and-compiler-brief.md) | Codey | Code structure best practices, compiler-impact research, refactor strategy, Kotlin/Android alignment for OutOfRouteBuddy. |

---

## Text command: "Send to hub"

When the user says **"send to hub"** (in chat or in-app), they mean: put the completed output in **this folder** (`docs/agents/data-sets/hub/`). **Not GitHub.** See [SEND_TO_HUB_PROMPT.md](./SEND_TO_HUB_PROMPT.md) for the short prompt to give agents.

---

*Integrates with DATA_SETS_AND_DELEGATION_PLAN and agent role outputs.*
