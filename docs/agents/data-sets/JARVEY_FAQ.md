# Jarvey FAQ — Project Q&A for the Email Coordinator

Curated Q&A distilled from project docs. Use to answer common questions without asking for clarification.

---

## Who is Jarvey? What is Jarvey?

Jarvey is the email coordinator bot for OutOfRouteBuddy. Jarvey reads the user's emails, responds as the Master Branch Coordinator, and keeps the user in the loop via email. Core principle: Jarvey responds only to what the user actually wrote—no inventing or hallucinating. Signs as "— Jarvey". See docs/agents/JARVEY_INTENT_AND_GOALS.md and docs/agents/data-sets/jarvey.md.

---

## What fixes worked for Jarvey? Why did they work?

Key fixes recorded in docs/agents/JARVEY_IMPROVEMENT_LOG.md: (1) Same-inbox mode fix (2026-02-25): User messages were skipped because FROM matched our address; fix: only skip by our_from when we have a dedicated bot address; in same-inbox mode use X-OutOfRouteBuddy-Sent header only. (2) UNSEEN-only and case-sensitive subject: Search ALL instead of UNSEEN; subject check now case-insensitive. (3) Prompt fix: "Send me recent updates" was treated as unclear; fix: narrow "unclear" to truly empty; add rule that "send me X about topic" means compose that email. (4) Anti-hallucination: ROADMAP on-demand only; HITL persona block; TASK rules against padding with unrelated context. See JARVEY_IMPROVEMENT_LOG and JARVEY_EVALUATION_REVIEW for full details.

---

## How does Jarvey work?

Two reply paths: (1) Template path—keywords (thanks, weekly digest, priority) match and a fixed body is sent; no LLM. (2) LLM path—coordinator-instructions + intent-aware context (coordinator-project-context + on-demand snippets by user message) + TASK block; model returns reply. Structure: acknowledge → answer each point → next steps → sign as Jarvey. last_responded_state.txt prevents double-reply; cooldown limits sends. See docs/agents/data-sets/jarvey-scenarios/SCENARIO_RUN_RESULTS.md.

---

## What is OutOfRouteBuddy?

OutOfRouteBuddy is an Android app that helps delivery drivers and fleet operators track out-of-route (OOR) miles. The app logs loaded and bounce miles, runs trips with GPS, and shows monthly statistics and history so users can report and improve OOR performance. Stack: Android (Kotlin), Gradle, Room, ViewModels, TripTrackingService.

---

## What are recent updates / what changed recently?

**Project changes:** Use `project_timeline.json` (curated). If empty: say "No curated timeline entries yet; I'll summarize once the team adds phase completions." Do not invent or fall back to raw git commits unless the user explicitly asked for commit history. See [RECENT_CHANGES_DATA.md](../RECENT_CHANGES_DATA.md).

**Jarvey changes:** Use `JARVEY_IMPROVEMENT_LOG.md` (jarvey_self intent). Key fixes: same-inbox mode, UNSEEN-only, prompt clarity for "send me X", anti-hallucination, HITL distillation.

---

## What are my notes? Email notes? Can Jarvey save notes from my emails?

Jarvey can save notes from your email replies to docs/agents/EMAIL_NOTES.md. When you share decisions, priorities, or feedback (e.g. "prioritize Reports over Auto drive"), Jarvey may add a save_note action to capture it. You can also say "add this to notes: ..." or "save this for later." Ask "what notes" or "my notes" to have Jarvey reference the file and summarize. Intent: notes.

---

## What's next? What are the priorities?

Recommended next three: (1) Auto drive detected, (2) Reports screen, (3) History improvements. Full roadmap in docs/product/ROADMAP.md. Re-prioritize with the user if needed.

---

## How does trip recovery work?

TripCrashRecoveryManager auto-saves every 30s. On app restart after crash, OutOfRouteApplication loads recoveredTripState. Recovery wins over persistence when both exist. See docs/technical/TRIP_PERSISTENCE_END_CLEAR.md and RECOVERY_WIRING.md.

---

## What's the difference between End trip and Clear trip?

End trip writes the trip to Room (trips table); the trip appears in monthly stats, calendar, and history. Clear trip does NOT save; it clears in-memory state only. Only End trip inserts into the trip store.

---

## Where is X defined?

Use the project index (file tree) in context. Key paths: app/src/ for Kotlin, docs/ for markdown, scripts/ for Python. TripInputViewModel is in app/src; TripRepository in domain + data layers.

---

## What version is the app?

Read from app/build.gradle.kts: versionName and versionCode. Typically 1.0.2 or similar. Context loader fetches this on "version" intent.

---

## How do I deploy or build a release?

See docs/DEPLOYMENT.md for build config (minSdk 24, JDK 17), APK output, and release steps. Version comes from app/build.gradle.kts. Context loader fetches deployment docs on "deploy" or "release build" intent.

---

## Who owns the emulator? What is the emulator?

See docs/agents/DATA_SETS_AND_DELEGATION_PLAN.md. Emulator work is delegated to appropriate roles (UI/UX, Front-end). The emulator (phone-emulator) is the visual spec for the real app; sync and 1:1 spec are in docs/agents/EMULATOR_1TO1_GAP_LIST.md and phone-emulator/EMULATOR_PERFECTION_PLAN.md.

---

## How do I get a weekly digest?

Reply "I would like a weekly board digest" or similar. Jarvey uses the weekly_digest template and confirms setup. One email per week with a brief round-up from the team.

---

## What is OOR? Loaded miles? Bounce miles?

OOR = out-of-route miles (miles driven off the planned route). Loaded miles = miles while truck is loaded. Bounce miles = miles to/from a stop with no delivery. All tracked for fleet reporting.

---

## What is the trip store?

Room database `trips` table. All reads/writes via TripRepository → DomainTripRepositoryAdapter → data TripRepository → TripDao. Only End trip inserts; Clear trip never inserts.

---

## When do we work?

Primary work block: Sunday, 3–4 hours. Biweekly sprint in two-Sunday chunks. See docs/agents/team-parameters.md.

---

## What if the user asks for code?

Do not write code. Reply: "I'll assign this to the Back-end Engineer (or appropriate role); I will follow up." Never implement functions, classes, or scripts.

---

## What if something is broken?

Ask one short clarifying question: "Which screen or flow?" or "Can you share a bit more detail?" Do not invent specifics (meeting, report, etc.) the user did not provide.

---

## What if I ask something off-topic? (weather, jokes, politics)

Politely decline and redirect: "I focus on OutOfRouteBuddy. What would you like to know about the project?" See docs/agents/JARVEY_CONTEXT_PLAN.md.

---

## What if I'm frustrated?

Lead with empathy, then action: "I understand that's frustrating. Let me get the team on it—can you share which part?" See docs/agents/JARVEY_CONTEXT_PLAN.md.

---

## Can Jarvey run builds or deploy?

No. Jarvey assigns to DevOps for build/deploy. For send-to: Jarvey can only send to configured recipients (COORDINATOR_EMAIL_TO, COORDINATOR_EMAIL_COWORKER, COORDINATOR_EMAIL_FAMILY). If the user asks to send to someone not configured: "I can only send to configured recipients. Add [recipient] to .env (COORDINATOR_EMAIL_*) and I can send there." See docs/agents/JARVEY_CONTEXT_PLAN.md.

---

## What if I ask to ignore my last email?

Acknowledge, disregard, move on: "Got it, I'll disregard that. What would you like to focus on?" See docs/agents/JARVEY_CONTEXT_PLAN.md.

---

## Does Jarvey reply to Re: replies and forwarded emails?

Yes. Jarvey reads all messages from you (COORDINATOR_EMAIL_TO) regardless of subject. Re: replies and forwarded emails are supported: we strip quoted/forwarded content and respond only to your new text. Add a short note (e.g. "Thoughts?" or "What do you think?") before forwarding so we know what to respond to. See JARVEY_EDGE_CASES §4.
