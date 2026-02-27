#!/usr/bin/env python3
"""
OutOfRouteBuddy Emulator Sync Service

Listens for POST /sync with design state JSON and writes changes to the project's
app/src/main/res/values/strings.xml. Run this locally, then use "Sync to project"
in the emulator to push edits to the codebase.

Usage:
  set OORB_PROJECT_ROOT=c:\path\to\Out-of-route-buddy   (optional; auto-detected if run from repo)
  python sync_service.py

Or run start_sync_service.bat (sets project root automatically).
"""

import json
import os
import re
import sys
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse

# Same mapping as phone-emulator/cursor-exporter.js
EMULATOR_TO_PROJECT = {
    "toolbar.title": {"file": "app/src/main/res/values/strings.xml", "stringName": "oor"},
    "loadedMiles.hint": {"file": "app/src/main/res/values/strings.xml", "stringName": "loaded_miles"},
    "bounceMiles.hint": {"file": "app/src/main/res/values/strings.xml", "stringName": "bounce_miles"},
    "startButton.text": {"file": "app/src/main/res/values/strings.xml", "stringName": "start_trip"},
    "todaysInfo.title": {"file": "app/src/main/res/values/strings.xml", "stringName": "todays_info"},
    "totalMiles.label": {"file": "app/src/main/res/values/strings.xml", "stringName": "total_miles"},
    "oorMiles.label": {"file": "app/src/main/res/values/strings.xml", "stringName": "oor_miles"},
    "oorPercent.label": {"file": "app/src/main/res/values/strings.xml", "stringName": "oor_percent"},
    "statisticsButton.text": {"file": "app/src/main/res/values/strings.xml", "stringName": "statistics"},
    "statisticsPeriod.label": {"file": "app/src/main/res/values/strings.xml", "stringName": "statistics_period_label"},
    "statisticsPeriod.button": {"file": "app/src/main/res/values/strings.xml", "stringName": "statistics_change_period_button"},
    "statisticsPeriod.value": {"file": "app/src/main/res/values/strings.xml", "stringName": "statistics_period_value"},
    "monthlyStats.title": {"file": "app/src/main/res/values/strings.xml", "stringName": "monthly_statistics"},
}

SYNC_PORT = int(os.environ.get("OORB_SYNC_PORT", "8765"))

# Max request body size (bytes) - prevents DoS / memory exhaustion
MAX_REQUEST_BODY_SIZE = 64 * 1024  # 64KB

# Audit tag for sync requests - filterable for log aggregation
SYNC_AUDIT_TAG = "SyncServiceAudit"


def get_value_at_path(obj, path):
    for key in path.split("."):
        obj = obj.get(key) if isinstance(obj, dict) else None
        if obj is None:
            return None
    return obj


def get_paths_from_dict(obj: dict, prefix: str = "") -> list[str]:
    """Extract all leaf paths from nested dict (e.g. 'toolbar.title', 'loadedMiles.hint')."""
    paths = []
    for key, value in obj.items():
        path = f"{prefix}.{key}" if prefix else key
        if isinstance(value, dict):
            paths.extend(get_paths_from_dict(value, path))
        else:
            paths.append(path)
    return paths


def validate_design_keys(design: dict) -> bool:
    """Reject design if it contains any path not in EMULATOR_TO_PROJECT (key allowlist)."""
    if not isinstance(design, dict):
        return False
    paths = get_paths_from_dict(design)
    for path in paths:
        if path not in EMULATOR_TO_PROJECT:
            return False
    return True


def escape_xml_value(s):
    if s is None:
        return ""
    s = str(s)
    return (
        s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
        .replace("'", "&#39;")
    )


def apply_design_to_strings_xml(project_root: str, design: dict) -> list[str]:
    """Apply design state to strings.xml. Returns list of applied change descriptions."""
    rel_path = "app/src/main/res/values/strings.xml"
    file_path = os.path.join(project_root, rel_path.replace("/", os.sep))
    applied = []

    # Build string name -> value from design (only mapped keys)
    updates = {}
    for path, mapping in EMULATOR_TO_PROJECT.items():
        value = get_value_at_path(design, path)
        if value is not None:
            updates[mapping["stringName"]] = value

    if not updates:
        return []

    os.makedirs(os.path.dirname(file_path), exist_ok=True)

    if os.path.isfile(file_path):
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
    else:
        content = '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n</resources>'

    # Ensure we have <resources>...</resources>
    if "<resources" not in content:
        content = '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n</resources>'

    for string_name, value in updates.items():
        escaped = escape_xml_value(value)
        new_line = f'    <string name="{string_name}">{escaped}</string>'
        pattern = re.compile(
            r'(\s*)<string\s+name="' + re.escape(string_name) + r'"\s*>.*?</string>',
            re.DOTALL,
        )
        if pattern.search(content):
            content = pattern.sub(new_line, content, count=1)
        else:
            # Insert before </resources>
            content = content.replace("</resources>", f"{new_line}\n</resources>")
        applied.append(f"{string_name}={value!r}")

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(content)

    return applied


def read_strings_xml(project_root: str) -> dict:
    """Read strings.xml and return dict string_name -> value."""
    rel_path = "app/src/main/res/values/strings.xml"
    file_path = os.path.join(project_root, rel_path.replace("/", os.sep))
    result = {}
    if not os.path.isfile(file_path):
        return result
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()
    # Match <string name="key">value</string>
    for m in re.finditer(r'<string\s+name="([^"]+)"[^>]*>([^<]*)</string>', content):
        name, value = m.group(1), m.group(2)
        value = value.replace("\\'", "'").replace("&quot;", '"').replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")
        result[name] = value
    return result


def design_from_strings(strings_dict: dict) -> dict:
    """Build design state object from string name -> value (for Load from project)."""
    design = {}
    for path, mapping in EMULATOR_TO_PROJECT.items():
        string_name = mapping["stringName"]
        value = strings_dict.get(string_name)
        if value is None:
            continue
        parts = path.split(".")
        obj = design
        for i, key in enumerate(parts):
            if i == len(parts) - 1:
                obj[key] = value
            else:
                if key not in obj:
                    obj[key] = {}
                obj = obj[key]
    return design


class SyncHandler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        print("[Sync]", format % args)

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()

    def do_GET(self):
        if self.path != "/design" and not self.path.startswith("/design?"):
            self.send_response(404)
            self.end_headers()
            return
        project_root = os.environ.get("OORB_PROJECT_ROOT", "").strip()
        if not project_root or not os.path.isdir(project_root):
            self.send_response(500)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(
                json.dumps({"ok": False, "error": "OORB_PROJECT_ROOT not set or not a directory."}).encode()
            )
            return
        try:
            strings_dict = read_strings_xml(project_root)
            design = design_from_strings(strings_dict)
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(json.dumps({"ok": True, "design": design}).encode())
        except Exception as e:
            self.send_response(500)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(json.dumps({"ok": False, "error": str(e)}).encode())

    def do_POST(self):
        if self.path != "/sync" and not self.path.startswith("/sync?"):
            self.send_response(404)
            self.end_headers()
            return

        content_length = int(self.headers.get("Content-Length", 0))
        if content_length > MAX_REQUEST_BODY_SIZE:
            self.send_response(413)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(
                json.dumps(
                    {
                        "ok": False,
                        "error": f"Request body too large (max {MAX_REQUEST_BODY_SIZE} bytes).",
                    }
                ).encode()
            )
            return

        try:
            body = self.rfile.read(content_length)
            design = json.loads(body.decode("utf-8"))
        except Exception as e:
            self.send_response(400)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(json.dumps({"ok": False, "error": str(e)}).encode())
            return

        if not validate_design_keys(design):
            self.send_response(400)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(
                json.dumps(
                    {
                        "ok": False,
                        "error": "Design contains keys not in allowlist (EMULATOR_TO_PROJECT).",
                    }
                ).encode()
            )
            return

        project_root = os.environ.get("OORB_PROJECT_ROOT", "").strip()
        if not project_root or not os.path.isdir(project_root):
            self.send_response(500)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(
                json.dumps(
                    {
                        "ok": False,
                        "error": "OORB_PROJECT_ROOT not set or not a directory. Run start_sync_service.bat or set it to your repo root.",
                    }
                ).encode()
            )
            return

        try:
            applied = apply_design_to_strings_xml(project_root, design)
            # Audit trail: who changed strings (filterable tag for log aggregation)
            keys_changed = ",".join(a.split("=", 1)[0] for a in applied) if applied else "none"
            print(f"[{SYNC_AUDIT_TAG}] sync_requested applied={len(applied)} keys={keys_changed}")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(
                json.dumps({"ok": True, "applied": applied, "count": len(applied)}).encode()
            )
        except Exception as e:
            self.send_response(500)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(json.dumps({"ok": False, "error": str(e)}).encode())


def main():
    project_root = os.environ.get("OORB_PROJECT_ROOT", "").strip()
    if not project_root:
        # Default: assume we're in scripts/emulator-sync-service, repo root is ../..
        script_dir = os.path.dirname(os.path.abspath(__file__))
        project_root = os.path.normpath(os.path.join(script_dir, "..", ".."))
        if os.path.isdir(project_root):
            os.environ["OORB_PROJECT_ROOT"] = project_root
            print(f"[Sync] Using project root: {project_root}")

    print(f"[Sync] Listening on http://127.0.0.1:{SYNC_PORT}/sync")
    print("[Sync] Use 'Sync to project' in the emulator to push edits. Press Ctrl+C to stop.")
    server = HTTPServer(("127.0.0.1", SYNC_PORT), SyncHandler)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n[Sync] Stopped.")
        server.shutdown()


if __name__ == "__main__":
    main()
