# Self-Improvement Plan — OutOfRouteBuddy

**Established:** 2025-03-11  
**Trigger:** Run when you direct it (on demand)  
**Focus:** App (features, UX, reliability) + Codebase (quality, tests, architecture)

---

## North Star

**Mission:** OutOfRouteBuddy gives drivers advanced analytics and tracking for out-of-route miles.

**Success:** Downloads, useful data for users, iPhone requests.

**Never:** Social features, ads, cloud-first.

---

## Self-Improvement Pillars

### Pillar 1: App (features, UX, reliability)

| Area | Actions |
|------|---------|
| **Ship readiness** | Store checklist, versioning, changelog, screenshots |
| **Polish** | Accessibility, copy, loading states, error messages |
| **Reliability** | Crash recovery, trip persistence, GPS edge cases |
| **New features** | Auto drive, reports screen, history → details (per ROADMAP) |

### Pillar 2: Codebase (quality, tests, architecture)

| Area | Actions |
|------|---------|
| **Quality** | Dead code cleanup, BuildConfig alignment, lint |
| **Tests** | Fix ignored tests, raise coverage on critical paths |
| **Architecture** | Repository chain, SSOT, no new persistence paths |
| **Security** | No PII in logs, FileProvider scope, Keystore for offline key |

---

## How to Run

**When you say:** "Run self-improvement plan" or "Self-improvement" or similar:

1. **Read** `docs/GOAL_AND_MISSION.md` and this plan
2. **Pick** 2–4 items across Pillar 1 and Pillar 2 (balanced)
3. **Execute** using the Improvement Loop routine (complete tasks on todo lists) or ad-hoc if open-ended
4. **Log** what was done in `docs/automation/IMPROVEMENT_LOOP_SUMMARY_<date>.md` or a brief note

---

## Suggested Items (from CRUCIAL + ROADMAP)

**App:**
- [ ] Statistics monthly-only (if you approve)
- [ ] Reports screen (period, export, share)
- [ ] Auto drive detected (one-tap start)
- [ ] History → trip details navigation
- [ ] Store checklist completion

**Codebase:**
- [ ] Resolve build issues (AAPT, dependencies)
- [ ] Dead code cleanup (REDUNDANT_DEAD_CODE_REPORT)
- [ ] LocationValidationServiceTest fix or document
- [ ] Gradle 9 readiness
- [ ] Security: PII audit, Keystore migration note

---

## Integration with Improvement Loop

The Improvement Loop (`docs/automation/IMPROVEMENT_LOOP_ROUTINE.md`) already covers quick wins, security, smoothness, and UI polish. Use it when you want a **time-boxed** self-improvement run. This plan is the **strategic layer**—what to prioritize when you direct a run.

---

*Run when you direct it. Update priorities as the project evolves.*
