# Cyber Security Loop — Research Phase

**Purpose:** Checklist for Phase 0 (Research) of the Cyber Security Loop. Discover new attacks, defenses, and techniques.

**When:** Start of Cyber Security Loop, or when Improvement Loop has Security focus.

---

## Research Checklist

| Task | Action |
|------|--------|
| **Web search** | "OWASP LLM Top 10 2025", "MITRE ATLAS techniques", "prompt injection techniques 2025" |
| **Update ATTACK_LIBRARY** | Add 1–2 new playbooks per loop from research |
| **CVE check** | Align with SECURITY_NOTES §11; run `./gradlew dependencyUpdates` or document last audit |
| **Review past exercises** | Read `docs/agents/data-sets/security-exercises/` for gaps |

---

## Sources

| Source | Use |
|--------|-----|
| [OWASP Top 10 for LLM Applications](https://owasp.org/www-project-top-10-for-large-language-model-applications) | Prompt injection, output handling |
| [MITRE ATLAS](https://atlas.mitre.org/) | AI/LLM threat techniques |
| [MITRE ATT&CK](https://attack.mitre.org/) | Enterprise/mobile attack techniques |
| [Promptfoo](https://www.promptfoo.dev/docs/guides/llm-redteaming/) | LLM red teaming strategies |

---

## Best Practices (from 2024–2025 research)

| Practice | Source | Application |
|----------|--------|--------------|
| **Controlled attack scenarios** | TIBER-EU, Purple Team frameworks | Map threat intel → MITRE ATT&CK → assess coverage → test detection. Structured, not chaotic. |
| **Adversary emulation** | SANS, Malvik | Automated (Caldera, Atomic Red Team) for known TTPs; manual for full lifecycle. Our loop: validation + synthetic scenarios. |
| **Explicit expected outcomes** | OWASP WSTG | Each scenario has expected: reject/flag/pass. Assert in tests. |
| **Sandbox / staging only** | Wallarm Threat Replay | Test in safe environments; no production; remove harmful code before testing. |
| **Proof of work** | Purple team labs | Log results; training JSON for regression; compare runs over time. |

---

## Output

- Append findings to `docs/agents/data-sets/security-exercises/YYYY-MM-DD-research.md` (optional)
- Update ATTACK_LIBRARY with new playbook IDs
- Note in CYBER_SECURITY_LOOP_SUMMARY: "Research: X. New playbooks: Y."

---

*Integrates with CYBER_SECURITY_LOOP_ROUTINE Phase 0.*
