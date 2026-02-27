# Human-in-the-Loop Manager — Semi Simple

- **Role card:** `docs/agents/roles/human-in-the-loop-manager.md`
- **Prompt type:** Semi Simple

## Prompt (copy-paste)

```
The Coordinator says: 'Email the user that the Help & Info dialog fix is deployed and ask them to confirm it looks good on their device.' Draft the subject and body you'd send (you don't have to run the script).
```

## Look for (scoring)

scripts/coordinator-email/, send_email.py or README; draft only; no product decisions.

---

## Response

**Subject:** Help & Info dialog fix is live — please confirm on your device  

**Body:** We’ve deployed the fix for the Help & Info dialog. When you have a moment, could you open Settings → Help & Info and confirm it looks and scrolls correctly on your device? Reply with any issues or a quick “looks good.”  

(Draft only; I’d run the script in `scripts/coordinator-email/` to send.)

