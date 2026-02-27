# Email Editor / Market Guru — Semi Simple

- **Role card:** `docs/agents/roles/email-editor-market-guru.md`
- **Prompt type:** Semi Simple

## Prompt (copy-paste)

```
The Coordinator wants to ask the user to choose between two feature priorities. Draft a short email (subject + 2–3 sentences) that the Human-in-the-Loop Manager could send.
```

## Look for (scoring)

No send logic; clear copy; handoff to HITL for sending.

---

## Response

**Subject:** Quick decision: which feature should we prioritize?  

**Body:** We’ve narrowed things down to two options and need your input. Option A: [brief description]. Option B: [brief description]. Which do you prefer, or would you like to discuss both? Reply when you can and we’ll align the roadmap accordingly.  

(Hand off to **Human-in-the-Loop Manager** to send via the coordinator script; I only draft copy.)

