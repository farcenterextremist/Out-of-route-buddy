# Team brainstorm – Emulator, IMAP replies, debug

**Coordinator:** Convened full team for 30-minute brainstorm, then 30-minute execution on: (1) emulator, (2) IMAP reply-reading, (3) debug.

---

## 30-minute brainstorm (summary)

### Design / Creative Manager
- Emulator is a design preview tool; keep it aligned with real app so we don’t drift. Prioritize trip state (Start/End) and Pause so the flow feels real.
- IMAP: treat “reply from user” as the source of truth for workdays and preferences; keep subject/body simple so we can parse reliably.

### UI/UX Specialist
- Emulator: Add Pause button (icon only, right of Start when active), arrow on the right of “STATISTICS” (icon gravity textEnd), numeric-only inputs. Match real app so Copy-for-Cursor stays useful.
- Reply-reading: One clear output file (`last_reply.txt`) so coordinator can say “read that file” when user says they replied.

### Front-end Engineer
- Emulator: Trip state (idle / active / paused), Start ↔ End Trip button text, Pause/Play icon toggle. Use `inputmode="decimal"` and pattern for numbers. Fix stats arrow to be on the right (flex order or separate span).
- Event listeners: Avoid re-attaching on every `render()`; use event delegation on `app-content` for click so we don’t double-fire or leak.

### Back-end Engineer
- IMAP: Reuse `.env` for IMAP (same Gmail app password often works). Script should only read; no delete/send. Output to a single file with timestamp so we know when we last read.

### DevOps Engineer
- Run `read_replies.py` on demand (no cron yet). Document IMAP enablement in Gmail and required env vars. Keep `.env` out of repo.

### QA Engineer
- Emulator: After changes, manually test Start → Pause → Resume → End and stats expand/collapse. IMAP: Test with a real reply and confirm `last_reply.txt` content.

### Security Specialist
- IMAP script: Read-only, credentials from `.env` only. Don’t log message bodies to disk beyond `last_reply.txt` (user content). Rate limit: run only when user asks.

### Email Editor / Market Guru
- Outgoing subject line: keep “OutOfRouteBuddy” so replies are easy to filter (Re: OutOfRouteBuddy).

### File Organizer
- Put `read_replies.py` in `scripts/coordinator-email/` next to `send_email.py`. Output `last_reply.txt` in same folder. Update README and FUTURE checklist.

### Human-in-the-Loop Manager
- When user says “I replied,” coordinator runs `read_replies.py` and Human-in-the-Loop reads `last_reply.txt` to respond or update team-parameters.

### Coordinator (synthesis)
- **Emulator (30 min):** Trip state + Pause button, stats arrow right, numeric inputs, single event delegation for app-content clicks.
- **IMAP (30 min):** One script `read_replies.py`: connect IMAP, filter by subject (Re: OutOfRouteBuddy or [OutOfRouteBuddy]), read latest, write body to `last_reply.txt`, mark read. Document in README.
- **Debug:** Fix duplicate listeners in emulator (delegate); ensure .env load is robust.

---

## Execution (done)

- **IMAP:** Added `scripts/coordinator-email/read_replies.py` — connects to Gmail IMAP, finds latest reply (Re: + OutOfRouteBuddy), writes to `last_reply.txt`, marks read. README and coordinator/Human-in-the-Loop docs updated.
- **Emulator:** Trip state (idle/active/paused), Start/End Trip button, Pause button (⏸/▶) when active, progress bar on Start, arrow on right of STATISTICS, numeric inputs (`inputmode="decimal"`, `pattern`). Event delegation on `app-content` to avoid duplicate listeners. Input values (loaded/bounce miles) persist to design state before re-render.
- **Debug:** `.env` loaded with UTF-8 in send_email; `last_reply.txt` gitignored; Human-in-the-Loop instructed to run read_replies when user says they replied.
