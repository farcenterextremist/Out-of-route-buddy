#!/usr/bin/env python3
"""
Intent-aware context loader for Jarvey. Detects user intent from subject/body,
loads relevant internal docs and external data, and returns context capped at
COORDINATOR_MAX_CONTEXT_CHARS (default 12000). Includes full project index.
Set lower (e.g. 8000) for Ollama if context is too large.
"""

import fnmatch
import json
import os
import re
import subprocess

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))

_DEFAULT_MAX_CONTEXT_CHARS = 12000  # More context for project questions; set lower (e.g. 8000) for Ollama if needed


def _load_env() -> dict:
    """Load .env from coordinator-email dir. Returns dict of key=value."""
    env_path = os.path.join(SCRIPT_DIR, ".env")
    if not os.path.isfile(env_path):
        return {}
    env = {}
    try:
        with open(env_path, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    k, v = line.split("=", 1)
                    env[k.strip()] = v.strip().strip('"').strip("'")
    except OSError:
        pass
    return env


def _get_max_context_chars() -> int:
    """Read COORDINATOR_MAX_CONTEXT_CHARS from env or .env; default 8000 for Ollama."""
    env = _load_env()
    val = env.get("COORDINATOR_MAX_CONTEXT_CHARS") or os.environ.get("COORDINATOR_MAX_CONTEXT_CHARS", "")
    val = (val or "").strip()
    if not val:
        return _DEFAULT_MAX_CONTEXT_CHARS
    try:
        n = int(val)
        return max(2000, min(n, 50000))
    except ValueError:
        return _DEFAULT_MAX_CONTEXT_CHARS


MAX_PROJECT_CONTEXT_CHARS = _get_max_context_chars()

# Excluded dirs/patterns for project index (never include in index or snippets)
_INDEX_EXCLUDE_DIRS = {
    ".git",
    "build",
    ".gradle",
    "node_modules",
    "__pycache__",
    "out",
    "gen",
    "bin",
    ".idea",
    "captures",
    "test-results",
}
_INDEX_EXCLUDE_PATTERNS = (
    "*.pyc",
    "*.apk",
    "*.dex",
    "*.class",
    "*.keystore",
    "*.jks",
    ".env",
    "local.properties",
)
_INDEX_EXCLUDE_NAMES = {"last_reply.txt", "last_responded_state.txt", "last_sent_timestamp.txt"}

# Allowed extensions for project index
_INDEX_INCLUDE_EXT = {".md", ".kt", ".xml", ".py", ".ts", ".tsx", ".json", ".kts", ".gradle"}
COORDINATOR_PROJECT_CONTEXT_PATH = os.path.join(
    REPO_ROOT, "docs", "agents", "coordinator-project-context.md"
)
JARVEY_BRAIN_PATH = os.path.join(REPO_ROOT, "docs", "agents", "JARVEY_PROJECT_BRAIN.md")
JARVEY_BRAIN_CAP = 3500  # Bridge from user to project; intent map, entity lookup, golden patterns
KNOWN_TRUTHS_PATH = os.path.join(REPO_ROOT, "docs", "agents", "KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md")
ROADMAP_PATH = os.path.join(REPO_ROOT, "docs", "product", "ROADMAP.md")
PROJECT_TIMELINE_PATH = os.path.join(SCRIPT_DIR, "project_timeline.json")
PROJECT_TIMELINE_MAX_ENTRIES = 50
EMAIL_NOTES_PATH = os.path.join(REPO_ROOT, "docs", "agents", "EMAIL_NOTES.md")

PROJECT_INDEX_CAP = 2500
CORE_SUMMARY_CAP = 2000
SSOT_CONDENSED_CAP = 800
ROADMAP_CONDENSED_CAP = 600

TRUNCATE_SUFFIX = "\n\n[... truncated ...]"


def _truncate_at_boundary(text: str, max_chars: int) -> str:
    """
    Truncate text at a natural boundary (newline, period, space) to avoid cutting mid-word.
    Leaves room for TRUNCATE_SUFFIX. Returns text unchanged if len(text) <= max_chars.
    """
    if not text or len(text) <= max_chars:
        return text
    reserve = len(TRUNCATE_SUFFIX) + 5
    cut_at = max_chars - reserve
    if cut_at <= 0:
        return text[:max_chars] + TRUNCATE_SUFFIX
    # Prefer: newline > period+space > space
    for sep in ("\n", ". ", " "):
        pos = text.rfind(sep, 0, cut_at + 1)
        if pos > 0:
            return text[: pos + len(sep)].rstrip() + TRUNCATE_SUFFIX
    return text[:cut_at].rstrip() + TRUNCATE_SUFFIX


def _is_excluded_path(rel_path: str, name: str) -> bool:
    """Return True if path should be excluded from project index."""
    parts = rel_path.replace("\\", "/").split("/")
    for part in parts:
        if part in _INDEX_EXCLUDE_DIRS:
            return True
    if name in _INDEX_EXCLUDE_NAMES:
        return True
    for pat in _INDEX_EXCLUDE_PATTERNS:
        if fnmatch.fnmatch(name, pat):
            return True
    # Exclude sensitive files
    if name == ".env" or name == "google-services.json" or name == "local.properties":
        return True
    return False


def _get_project_index_paths() -> list[str]:
    """Return list of project file paths (for entity matching). Same exclusions as build_project_index."""
    paths = []
    try:
        for root, _dirs, files in os.walk(REPO_ROOT, topdown=True):
            rel_root = os.path.relpath(root, REPO_ROOT)
            if rel_root == ".":
                rel_root = ""
            _dirs[:] = [d for d in _dirs if d not in _INDEX_EXCLUDE_DIRS and not d.startswith(".")]
            for name in files:
                if _is_excluded_path(
                    os.path.join(rel_root, name).replace("\\", "/"), name
                ):
                    continue
                ext = os.path.splitext(name)[1].lower()
                if ext not in _INDEX_INCLUDE_EXT:
                    continue
                rel_path = os.path.join(rel_root, name).replace("\\", "/")
                paths.append(rel_path)
    except OSError:
        pass
    return paths


def build_project_index(cap_chars: int = PROJECT_INDEX_CAP) -> str:
    """
    Walk REPO_ROOT and build a compact file tree (paths only). Excludes build
    artifacts, secrets, and gitignore patterns. Cap output at cap_chars.
    """
    paths = _get_project_index_paths()
    lines = ["Project files (reference when answering \"where is X\"):"]
    current_len = len(lines[0]) + 1
    for p in sorted(paths):
        line = "  " + p
        if current_len + len(line) + 1 > cap_chars:
            lines.append("  [... more files truncated ...]")
            break
        lines.append(line)
        current_len += len(line) + 1
    return "\n".join(lines)


# Intent config: loaded from intents/intents.json, fallback to built-in
INTENTS_DIR = os.path.join(SCRIPT_DIR, "intents")
INTENTS_JSON = os.path.join(INTENTS_DIR, "intents.json")

# Built-in fallback if intents.json missing (minimal; intents.json is primary)
_INTENT_CONFIG_FALLBACK = [
    ("roadmap", ["what's next", "priorities", "roadmap"], [("docs/product/ROADMAP.md", 1200)]),
    ("recovery", ["recovery", "crash", "restart", "lost trip"], [("docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md", 2500), ("docs/technical/TRIP_PERSISTENCE_END_CLEAR.md", 800)]),
    ("persistence", ["persistence", "save", "clear", "end trip", "room", "database"], [("docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md", 2500)]),
    ("statistics", ["statistics", "monthly", "calendar", "period"], [("docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md", 2500)]),
    ("architecture", ["architecture", "wiring", "components"], [("docs/technical/WIRING_MAP.md", 1200)]),
    ("delegation", ["who owns", "delegation", "assign", "role"], [("docs/agents/DATA_SETS_AND_DELEGATION_PLAN.md", 1200)]),
    ("workdays", ["workdays", "sprint", "when do we work"], [("docs/agents/team-parameters.md", 800)]),
    ("version", ["version", "build", "latest"], [("version", 100)]),
    ("deployment", ["deploy", "deployment", "release", "minSdk", "JDK"], [("docs/DEPLOYMENT.md", 1200)]),
    ("recent", ["recent", "last commit", "what changed", "timeline"], [("project_timeline", 600)]),
    ("improvements", ["improvements", "crucial", "todo"], [("docs/CRUCIAL_IMPROVEMENTS_TODO.md", 600)]),
    ("emulator", ["emulator", "phone-emulator", "sync"], [("phone-emulator/EMULATOR_PERFECTION_PLAN.md", 800), ("docs/agents/EMULATOR_1TO1_GAP_LIST.md", 800)]),
    ("reports", ["reports", "export", "share"], [("docs/product/FEATURE_BRIEF_reports.md", 800)]),
    ("tests", ["tests", "qa", "test"], [("docs/qa/TEST_STRATEGY.md", 600), ("docs/qa/FAILING_OR_IGNORED_TESTS.md", 600)]),
    ("security", ["security", "threat"], [("docs/security/SECURITY_PLAN.md", 800), ("docs/security/SECURITY_NOTES.md", 1000)]),
    ("feature_briefs", ["auto drive", "feature brief"], [("docs/product/FEATURE_BRIEF_auto_drive.md", 800)]),
    ("faq", ["how does", "what is", "explain", "clarify"], [("docs/agents/data-sets/JARVEY_FAQ.md", 2000)]),
    ("glossary", ["define", "meaning of", "term", "glossary"], [("docs/agents/JARVEY_GLOSSARY.md", 500)]),
    ("send_to", ["send to coworker", "email family", "forward to"], [("docs/agents/JARVEY_RECIPIENT_ALIASES.md", 300)]),
    ("recommend", ["recommend", "video", "tutorial", "learn", "resource"], [("recommend", 1500)]),
    ("jarvey_self", ["who is jarvey", "what is jarvey", "about jarvey", "jarvey fix", "what fixes worked"], [("docs/agents/JARVEY_IMPROVEMENT_LOG.md", 1500), ("docs/agents/data-sets/JARVEY_EVALUATION_REVIEW.md", 1000)]),
]


def _load_intent_config() -> list[tuple[str, list[str], list[tuple[str, int]]]]:
    """Load INTENT_CONFIG from intents/intents.json or use fallback."""
    if not os.path.isfile(INTENTS_JSON):
        return _INTENT_CONFIG_FALLBACK
    try:
        with open(INTENTS_JSON, encoding="utf-8") as f:
            data = json.load(f)
        if not isinstance(data, list):
            return _INTENT_CONFIG_FALLBACK
        result = []
        for item in data:
            if not isinstance(item, dict):
                continue
            name = item.get("name")
            keywords = item.get("keywords", [])
            sources_raw = item.get("sources", [])
            sources = []
            for s in sources_raw:
                if isinstance(s, (list, tuple)) and len(s) >= 2:
                    sources.append((str(s[0]), int(s[1])))
                elif isinstance(s, dict) and "path" in s:
                    sources.append((s["path"], int(s.get("cap", 1000))))
            if name and keywords is not None:
                result.append((name, list(keywords), sources))
        return result if result else _INTENT_CONFIG_FALLBACK
    except (json.JSONDecodeError, OSError):
        return _INTENT_CONFIG_FALLBACK


# Lazy-loaded; use get_intent_config() for access
_INTENT_CONFIG_CACHE: list[tuple[str, list[str], list[tuple[str, int]]]] | None = None


def get_intent_config() -> list[tuple[str, list[str], list[tuple[str, int]]]]:
    """Return INTENT_CONFIG, loading from file if needed."""
    global _INTENT_CONFIG_CACHE
    if _INTENT_CONFIG_CACHE is None:
        _INTENT_CONFIG_CACHE = _load_intent_config()
    return _INTENT_CONFIG_CACHE


def reload_intents() -> None:
    """Reload intents from file (for hot-reload in dev)."""
    global _INTENT_CONFIG_CACHE
    _INTENT_CONFIG_CACHE = None
    get_intent_config()


def detect_intents(subject: str, body: str) -> list[str]:
    """Detect intent tags from subject and body via keyword matching. Returns list of intent names."""
    combined = f"{subject or ''} {body or ''}".lower()
    intents = []
    for intent_name, keywords, _ in get_intent_config():
        if any(kw in combined for kw in keywords):
            intents.append(intent_name)
    return intents


# Regex patterns for entity extraction (e.g. "where is TripInputViewModel")
_ENTITY_PATTERNS = [
    (r"where\s+is\s+(\w+)", 1),
    (r"who\s+owns\s+(\w+)", 1),
    (r"tell\s+me\s+about\s+(\w+)", 1),
    (r"(\w+ViewModel)", 1),
    (r"(\w+Repository)", 1),
    (r"(\w+Manager)", 1),
]
# Known terms from INTENT_CONFIG to extract when they appear as standalone nouns
_ENTITY_KEYWORDS = ["emulator", "recovery", "roadmap", "reports", "persistence"]


def extract_entities(subject: str, body: str) -> list[str]:
    """
    Extract entity names from subject and body via regex and keyword matching.
    Returns unique list, capped at 5. Used for finer context loading (e.g. path hints).
    """
    combined = f"{subject or ''} {body or ''}"
    combined_lower = combined.lower()
    entities = []
    seen = set()
    for pattern, group in _ENTITY_PATTERNS:
        for m in re.finditer(pattern, combined, re.IGNORECASE):
            ent = m.group(group).strip()
            if ent and len(ent) > 2 and ent.lower() not in seen:
                entities.append(ent)
                seen.add(ent.lower())
    for kw in _ENTITY_KEYWORDS:
        if kw in combined_lower and kw not in seen:
            entities.append(kw)
            seen.add(kw)
    return entities[:5]


def _entity_path_hints(entities: list[str]) -> str:
    """Match entities to project index paths; return formatted hint lines."""
    if not entities:
        return ""
    paths = _get_project_index_paths()
    lines = []
    for ent in entities:
        ent_clean = ent.replace("ViewModel", "").replace("Repository", "").replace("Manager", "")
        for p in paths:
            if ent in p or ent_clean in p:
                lines.append(f"Entity {ent} is defined at: {p}")
                break
    if not lines:
        return ""
    return "Entity location hints:\n" + "\n".join(lines)


def _get_app_version() -> str:
    """Read app/build.gradle.kts for versionName and versionCode. Returns one-line summary or empty string."""
    path = os.path.join(REPO_ROOT, "app", "build.gradle.kts")
    try:
        if not os.path.isfile(path):
            return ""
        with open(path, encoding="utf-8") as f:
            content = f.read()
        version_name = ""
        version_code = ""
        m = re.search(r'versionName\s*=\s*["\']([^"\']+)["\']', content)
        if m:
            version_name = m.group(1)
        m = re.search(r"versionCode\s*=\s*(\d+)", content)
        if m:
            version_code = m.group(1)
        if version_name or version_code:
            return f"App version: {version_name or '?'} (versionCode {version_code or '?'})"
        return ""
    except OSError:
        return ""


def _get_recent_commits() -> str:
    """Run git log -5 --oneline. Returns formatted output or empty string on failure."""
    try:
        result = subprocess.run(
            ["git", "log", "-5", "--oneline"],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            timeout=5,
        )
        if result.returncode == 0 and result.stdout.strip():
            lines = result.stdout.strip().split("\n")
            return "Recent commits:\n" + "\n".join("  • " + ln for ln in lines)
        return ""
    except (subprocess.SubprocessError, FileNotFoundError, OSError):
        return ""


def _get_git_status(cap: int = 300) -> str:
    """Run git status -sb and git log -1 --oneline. Returns branch, last commit, uncommitted count."""
    try:
        status = subprocess.run(
            ["git", "status", "-sb"],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            timeout=5,
        )
        log = subprocess.run(
            ["git", "log", "-1", "--oneline"],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            timeout=5,
        )
        parts = []
        if status.returncode == 0 and status.stdout.strip():
            parts.append("Git status:\n" + status.stdout.strip().split("\n")[0])
        if log.returncode == 0 and log.stdout.strip():
            parts.append("Last commit: " + log.stdout.strip())
        result = "\n".join(parts)
        if len(result) > cap:
            result = result[: cap - 25] + "\n[... truncated ...]"
        return result
    except (subprocess.SubprocessError, FileNotFoundError, OSError):
        return ""


def _get_app_structure_summary(cap: int = 800) -> str:
    """
    Walk app/src/main/java/com/example/outofroutebuddy/ and group by top-level dir.
    Returns formatted summary: "App structure:\n- data: TripRepository, TripDao, ..."
    """
    app_dir = os.path.join(REPO_ROOT, "app", "src", "main", "java", "com", "example", "outofroutebuddy")
    if not os.path.isdir(app_dir):
        return ""
    groups: dict[str, list[str]] = {}
    for root, dirs, files in os.walk(app_dir, topdown=True):
        rel = os.path.relpath(root, app_dir)
        if rel == ".":
            for f in files:
                if f.endswith(".kt"):
                    name = os.path.splitext(f)[0]
                    groups.setdefault("(root)", []).append(name)
            continue
        parts = rel.replace("\\", "/").split("/")
        top = parts[0]
        for f in files:
            if f.endswith(".kt"):
                name = os.path.splitext(f)[0]
                groups.setdefault(top, []).append(name)
    lines = ["App structure:"]
    for top in sorted(groups.keys(), key=lambda x: (x == "(root)", x)):
        classes = groups[top][:5]
        suffix = ", ..." if len(groups[top]) > 5 else ""
        lines.append(f"- {top}: {', '.join(classes)}{suffix}")
    result = "\n".join(lines)
    if len(result) > cap:
        result = result[: cap - 25] + "\n[... truncated ...]"
    return result


def get_project_timeline(cap_chars: int = 600) -> str:
    """
    Read project_timeline.json, merge with live git log, return formatted timeline (newest first).
    Capped at cap_chars. Stored entries are listed first, then recent commits.
    """
    parts = []
    # 1. Stored timeline entries (phase completions, manual entries)
    if os.path.isfile(PROJECT_TIMELINE_PATH):
        try:
            with open(PROJECT_TIMELINE_PATH, encoding="utf-8") as f:
                entries = json.load(f)
            if isinstance(entries, list) and entries:
                lines = ["Project timeline (notable changes):"]
                current_len = len(lines[0]) + 1
                # Newest first (entries are appended, so reverse)
                for entry in reversed(entries[-PROJECT_TIMELINE_MAX_ENTRIES:]):
                    if not isinstance(entry, dict):
                        continue
                    date = entry.get("date", "?")
                    title = entry.get("title", "")
                    etype = entry.get("type", "")
                    line = f"  {date} [{etype}] {title}"
                    if current_len + len(line) + 1 > cap_chars:
                        lines.append("  [... more entries truncated ...]")
                        break
                    lines.append(line)
                    current_len += len(line) + 1
                if len(lines) > 1:
                    parts.append("\n".join(lines))
        except (json.JSONDecodeError, OSError):
            pass
    # 2. Live git log
    git_part = _get_recent_commits()
    if git_part:
        if len(git_part) > cap_chars // 2:
            cut = git_part[: cap_chars // 2 - 30]
            last_nl = cut.rfind("\n")
            git_part = (cut[: last_nl + 1] if last_nl >= 0 else cut) + "  (More commits in repo.)"
        parts.append(git_part)
    if not parts:
        return ""
    result = "\n\n".join(parts)
    if len(result) > cap_chars:
        cut = result[: cap_chars - 35]
        last_nl = cut.rfind("\n")
        result = (cut[: last_nl + 1] if last_nl > 0 else cut) + "\n(More in project history.)"
    return result


def get_project_timeline_curated(cap_chars: int = 600) -> str:
    """
    Return only curated timeline entries (no git). For "recent" intent.
    When timeline is empty, return instruction so model does not output raw git commits.
    """
    parts = []
    if os.path.isfile(PROJECT_TIMELINE_PATH):
        try:
            with open(PROJECT_TIMELINE_PATH, encoding="utf-8") as f:
                entries = json.load(f)
            if isinstance(entries, list) and entries:
                lines = ["Project timeline (notable changes):"]
                current_len = len(lines[0]) + 1
                for entry in reversed(entries[-PROJECT_TIMELINE_MAX_ENTRIES:]):
                    if not isinstance(entry, dict):
                        continue
                    date = entry.get("date", "?")
                    title = entry.get("title", "")
                    etype = entry.get("type", "")
                    line = f"  {date} [{etype}] {title}"
                    if current_len + len(line) + 1 > cap_chars:
                        lines.append("  [... more entries truncated ...]")
                        break
                    lines.append(line)
                    current_len += len(line) + 1
                if len(lines) > 1:
                    parts.append("\n".join(lines))
        except (json.JSONDecodeError, OSError):
            pass
    if not parts:
        return (
            "Project timeline: No curated entries yet. When the user asks for 'recent changes', "
            "say you'll summarize once the team adds phase completions. Do not output raw git commits."
        )
    result = "\n\n".join(parts)
    if len(result) > cap_chars:
        cut = result[: cap_chars - 35]
        last_nl = cut.rfind("\n")
        result = (cut[: last_nl + 1] if last_nl > 0 else cut) + "\n(More in project history.)"
    return result


def append_to_timeline(date: str, etype: str, title: str, detail: str = "") -> None:
    """
    Append an entry to project_timeline.json. Prunes to PROJECT_TIMELINE_MAX_ENTRIES.
    """
    entries = []
    if os.path.isfile(PROJECT_TIMELINE_PATH):
        try:
            with open(PROJECT_TIMELINE_PATH, encoding="utf-8") as f:
                entries = json.load(f)
        except (json.JSONDecodeError, OSError):
            pass
    if not isinstance(entries, list):
        entries = []
    entries.append({"date": date, "type": etype, "title": title, "detail": detail})
    if len(entries) > PROJECT_TIMELINE_MAX_ENTRIES:
        entries = entries[-PROJECT_TIMELINE_MAX_ENTRIES:]
    try:
        with open(PROJECT_TIMELINE_PATH, "w", encoding="utf-8") as f:
            json.dump(entries, f, indent=2)
    except OSError:
        pass


def append_to_notes(note: str, topic: str = "") -> None:
    """
    Append a note from user email to docs/agents/EMAIL_NOTES.md.
    Format: ## YYYY-MM-DD [topic]\n\n{note}\n\n
    """
    if not note or not note.strip():
        return
    from datetime import datetime
    date_str = datetime.utcnow().strftime("%Y-%m-%d")
    header = f"## {date_str}"
    if topic and topic.strip():
        header += f" — {topic.strip()}"
    header += "\n\n"
    block = header + note.strip() + "\n\n"
    try:
        with open(EMAIL_NOTES_PATH, "a", encoding="utf-8") as f:
            f.write(block)
    except OSError:
        pass


def get_email_notes(cap_chars: int = 1500) -> str:
    """Return contents of EMAIL_NOTES.md for context. Empty if file missing."""
    if not os.path.isfile(EMAIL_NOTES_PATH):
        return ""
    try:
        with open(EMAIL_NOTES_PATH, encoding="utf-8") as f:
            content = f.read().strip()
        if not content:
            return ""
        if len(content) > cap_chars:
            content = _truncate_at_boundary(content, cap_chars) + "\n\n(More in docs/agents/EMAIL_NOTES.md.)"
        return content
    except OSError:
        return ""


def get_capability_options_text(limit: int = 5) -> str:
    """
    Load jarvey_capability_menu.json and return formatted options for prompt injection.
    Returns "1. Roadmap status 2. Recent changes ..." or empty string on failure.
    """
    menu_path = os.path.join(SCRIPT_DIR, "jarvey_capability_menu.json")
    if not os.path.isfile(menu_path):
        return ""
    try:
        with open(menu_path, encoding="utf-8") as f:
            data = json.load(f)
        options = data.get("options") or []
        if not options:
            return ""
        lines = []
        for i, opt in enumerate(options[:limit], 1):
            label = (opt.get("label") or str(opt)).strip()
            if label:
                lines.append(f"{i}. {label}")
        return " ".join(lines) if lines else ""
    except (json.JSONDecodeError, OSError):
        return ""


def get_capability_label_for_number(num_str: str) -> str | None:
    """
    If num_str is "1" through "10", return the corresponding capability menu label.
    Otherwise return None.
    """
    menu_path = os.path.join(SCRIPT_DIR, "jarvey_capability_menu.json")
    if not os.path.isfile(menu_path):
        return None
    try:
        with open(menu_path, encoding="utf-8") as f:
            data = json.load(f)
        options = data.get("options") or []
        n = num_str.strip()
        if n not in ("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"):
            return None
        idx = int(n)
        if 1 <= idx <= len(options):
            opt = options[idx - 1]
            label = (opt.get("label") or str(opt)).strip()
            return label if label else None
    except (json.JSONDecodeError, OSError, ValueError):
        pass
    return None


def get_version_summary() -> str:
    """Public wrapper for version summary. Returns one-line app version or empty string."""
    return _get_app_version()


def get_roadmap_summary(cap: int = 600) -> str:
    """Return condensed roadmap for template replies. First cap chars of ROADMAP.md + note."""
    snippet = _read_file_snippet("docs/product/ROADMAP.md", cap)
    if not snippet:
        return "Roadmap not found. Ask in the next session for priorities."
    return snippet.rstrip() + "\n\nFull roadmap in docs/product/ROADMAP.md."


def _read_file_snippet(rel_path: str, cap: int) -> str:
    """Read file and return first cap chars. Returns empty string on failure."""
    path = os.path.join(REPO_ROOT, rel_path)
    try:
        if not os.path.isfile(path):
            return ""
        with open(path, encoding="utf-8") as f:
            content = f.read()
        content = content.strip()
        if len(content) > cap:
            content = _truncate_at_boundary(content, cap)
        return content
    except OSError:
        return ""


def load_snippet(intent: str) -> str:
    """
    Load snippet for a single intent. Maps intent to file(s) or external fetchers.
    Returns combined snippet string, truncated per source cap.
    """
    sources = []
    for intent_name, _, intent_sources in get_intent_config():
        if intent_name == intent:
            sources = intent_sources
            break

    if not sources:
        return ""

    parts = []
    for source, cap in sources:
        if source == "version":
            snippet = _get_app_version()
            if snippet and len(snippet) > cap:
                snippet = snippet[:cap]
        elif source == "git":
            snippet = _get_recent_commits()
            if snippet and len(snippet) > cap:
                snippet = snippet[:cap]
        elif source == "git_status":
            snippet = _get_git_status(cap=cap)
        elif source == "project_timeline":
            snippet = get_project_timeline(cap_chars=cap)
        elif source == "project_timeline_curated":
            snippet = get_project_timeline_curated(cap_chars=cap)
        elif source == "email_notes":
            snippet = get_email_notes(cap_chars=cap)
        elif source == "app_structure":
            snippet = _get_app_structure_summary(cap=cap)
        else:
            snippet = _read_file_snippet(source, cap)

        if snippet:
            parts.append(snippet)

    return "\n\n---\n\n".join(parts) if parts else ""


def _build_expanded_base() -> str:
    """
    Build expanded base context: core summary + project brain + project index + condensed SSOT.
    ROADMAP is loaded on-demand only when "roadmap" intent is detected.
    """
    parts = []
    # 1. Core summary (coordinator-project-context.md, cap 2000)
    if os.path.isfile(COORDINATOR_PROJECT_CONTEXT_PATH):
        with open(COORDINATOR_PROJECT_CONTEXT_PATH, encoding="utf-8") as f:
            core = f.read().strip()
        if len(core) > CORE_SUMMARY_CAP:
            core = _truncate_at_boundary(core, CORE_SUMMARY_CAP)
        parts.append(core)
    # 2. Project brain (bridge from user to project; intent map, entity lookup, golden patterns)
    if os.path.isfile(JARVEY_BRAIN_PATH):
        with open(JARVEY_BRAIN_PATH, encoding="utf-8") as f:
            brain = f.read().strip()
        if len(brain) > JARVEY_BRAIN_CAP:
            brain = _truncate_at_boundary(brain, JARVEY_BRAIN_CAP)
        parts.append(brain)
    # 3. Project index
    index = build_project_index(cap_chars=PROJECT_INDEX_CAP)
    if index:
        parts.append(index)
    # 4. SSOT condensed (first 800 chars)
    if os.path.isfile(KNOWN_TRUTHS_PATH):
        with open(KNOWN_TRUTHS_PATH, encoding="utf-8") as f:
            ssot = f.read().strip()
        if len(ssot) > SSOT_CONDENSED_CAP:
            ssot = _truncate_at_boundary(ssot, SSOT_CONDENSED_CAP)
        parts.append("SSOT (condensed):\n" + ssot)
    return "\n\n---\n\n".join(parts)


def load_context_for_user_message(subject: str, body: str) -> str:
    """
    Load expanded base context (core + index + SSOT) + on-demand snippets by intent.
    When JARVEY_CONVERSATION_MEMORY=1, prepends recent conversation for the thread.
    Total capped at COORDINATOR_MAX_CONTEXT_CHARS (default 8000).
    When COORDINATOR_FAST_CONTEXT_FOR_RECENT=1, uses reduced cap (4000) for narrow
    "recent" intent + short body to speed up Ollama.
    """
    max_chars = _get_max_context_chars()
    env = _load_env()

    # Optional: lighter context for "recent" intent when body is short (speeds up Ollama)
    if env.get("COORDINATOR_FAST_CONTEXT_FOR_RECENT", "").strip().lower() in ("1", "true", "yes"):
        intents_pre = detect_intents(subject or "", body or "")
        combined_len = len((subject or "") + " " + (body or ""))
        if intents_pre == ["recent"] and combined_len < 100:
            fast_cap = 4000
            try:
                val = env.get("COORDINATOR_FAST_CONTEXT_CHARS", "4000")
                if val:
                    fast_cap = int(val)
            except ValueError:
                pass
            max_chars = min(max_chars, max(2000, fast_cap))
    memory_enabled = env.get("JARVEY_CONVERSATION_MEMORY", "").strip().lower() in ("1", "true", "yes")
    max_exchanges = 5
    try:
        n = env.get("JARVEY_CONVERSATION_MAX_EXCHANGES", "5")
        if n:
            max_exchanges = int(n)
    except ValueError:
        pass

    parts = []
    if memory_enabled:
        try:
            from conversation_memory import load_history, get_thread_id
            thread_id = get_thread_id(subject or "", None)
            history = load_history(thread_id, max_exchanges=max_exchanges)
            if history:
                parts.append("Recent conversation:\n" + history)
        except Exception:
            pass

    base = _build_expanded_base()
    if parts:
        base = "\n\n---\n\n".join(parts) + "\n\n---\n\n" + base

    intents = detect_intents(subject or "", body or "")
    entities = extract_entities(subject or "", body or "")
    entity_hints = _entity_path_hints(entities)
    if entity_hints:
        base = base + "\n\n---\n\n" + entity_hints

    if not intents:
        if len(base) > max_chars:
            return base[:max_chars] + "\n\n[... truncated for context limit ...]"
        return base

    # Collect unique sources (path or "version"/"git") to avoid loading same file twice
    seen_sources = set()
    snippets = []
    for intent_name, _, intent_sources in get_intent_config():
        if intent_name not in intents:
            continue
        for source, cap in intent_sources:
            key = (source, cap)
            if key in seen_sources:
                continue
            seen_sources.add(key)
            if source == "version":
                snip = _get_app_version()
                if snip and len(snip) > cap:
                    snip = snip[:cap]
            elif source == "git":
                snip = _get_recent_commits()
                if snip and len(snip) > cap:
                    snip = snip[:cap]
            elif source == "git_status":
                snip = _get_git_status(cap=cap)
            elif source == "project_timeline":
                snip = get_project_timeline(cap_chars=cap)
            elif source == "project_timeline_curated":
                snip = get_project_timeline_curated(cap_chars=cap)
            elif source == "email_notes":
                snip = get_email_notes(cap_chars=cap)
            elif source == "app_structure":
                snip = _get_app_structure_summary(cap=cap)
            elif source == "recommend":
                try:
                    from jarvey_fetchers import fetch_recommendations
                    snip = fetch_recommendations(body or "")
                except Exception:
                    snip = ""
                if snip and len(snip) > cap:
                    snip = _truncate_at_boundary(snip, cap)
            elif source == "rag_search" and (env.get("JARVEY_RAG_ENABLED") or os.environ.get("JARVEY_RAG_ENABLED", "")).strip().lower() in ("1", "true", "yes"):
                try:
                    from jarvey_rag import search as rag_search, _get_top_k
                    query = f"{subject or ''} {body or ''}".strip()
                    chunks = rag_search(query, top_k=_get_top_k())
                    snip = "\n\n---\n\n".join(chunks) if chunks else ""
                    if snip and len(snip) > cap:
                        snip = _truncate_at_boundary(snip, cap)
                except Exception:
                    snip = ""
            else:
                snip = _read_file_snippet(source, cap)
            if snip:
                snippets.append(snip)

    if not snippets:
        if len(base) > max_chars:
            return base[:max_chars] + "\n\n[... truncated for context limit ...]"
        return base

    extra = "\n\n---\n\nAdditional context (on-demand):\n\n" + "\n\n---\n\n".join(snippets)
    combined = base + extra

    if len(combined) > max_chars:
        combined = combined[:max_chars] + "\n\n[... truncated for context limit ...]"

    return combined.strip()
