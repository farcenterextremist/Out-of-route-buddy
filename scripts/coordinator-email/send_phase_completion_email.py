#!/usr/bin/env python3
"""
Send a preset "big change" email automatically. Used so Phase A/B/C (or other
milestones) trigger an email without manual steps.
Usage: python send_phase_completion_email.py PRESET
Example: python send_phase_completion_email.py phase_abc
Requires .env (same as send_email.py). Body files live in this folder.
"""

import os
import sys
from datetime import date

# Presets: preset_name -> (subject, body_filename)
PRESETS = {
    "phase_abc": (
        "OutOfRouteBuddy: Emulator Phase A/B/C complete - summary",
        "phase_abc_completion_body.txt",
    ),
    # Add more presets here, e.g.:
    # "figma_phase": ("OutOfRouteBuddy: Figma-inspired editing complete - summary", "figma_completion_body.txt"),
}


def main():
    if len(sys.argv) < 2:
        print("Usage: python send_phase_completion_email.py PRESET", file=sys.stderr)
        print("Presets: " + ", ".join(PRESETS.keys()), file=sys.stderr)
        sys.exit(1)
    preset = sys.argv[1].strip().lower()
    if preset not in PRESETS:
        print(f"Unknown preset: {preset}. Available: {list(PRESETS.keys())}", file=sys.stderr)
        sys.exit(1)
    subject, body_filename = PRESETS[preset]
    script_dir = os.path.dirname(os.path.abspath(__file__))
    body_path = os.path.join(script_dir, body_filename)
    if not os.path.isfile(body_path):
        print(f"Body file not found: {body_path}", file=sys.stderr)
        sys.exit(1)
    with open(body_path, encoding="utf-8") as f:
        body = f.read()
    # Append to project timeline before sending
    try:
        from context_loader import append_to_timeline
        append_to_timeline(
            date=str(date.today()),
            etype="phase",
            title=subject,
            detail=f"Preset: {preset}",
        )
    except Exception:
        pass
    # Delegate to send_email
    from send_email import send
    try:
        send(subject, body)
        print("Email sent successfully.")
    except Exception as e:
        print(f"Failed to send email: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
