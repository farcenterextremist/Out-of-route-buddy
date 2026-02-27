#!/usr/bin/env python3
"""
Live fetchers for Jarvey: YouTube recommendations, optional web search.
Used when user asks for videos, tutorials, or resources. Requires API keys in .env.
"""

import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)


def _load_env() -> dict:
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


def fetch_youtube_recommendations(query: str, max_results: int = 5) -> str:
    """
    Search YouTube for videos matching the query. Returns formatted titles + URLs.
    Requires COORDINATOR_YOUTUBE_API_KEY in .env. Returns empty string if no key or on error.
    """
    api_key = _load_env().get("COORDINATOR_YOUTUBE_API_KEY", "").strip()
    if not api_key:
        return ""

    # Build search URL (YouTube Data API v3)
    q_encoded = urllib.parse.quote(query)
    url = (
        f"https://www.googleapis.com/youtube/v3/search"
        f"?part=snippet&type=video&maxResults={max_results}&q={q_encoded}&key={api_key}"
    )
    try:
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
    except (urllib.error.URLError, urllib.error.HTTPError, json.JSONDecodeError, OSError):
        return ""

    items = data.get("items", [])
    if not items:
        return ""

    lines = ["YouTube recommendations:"]
    for item in items:
        vid_id = item.get("id", {}).get("videoId")
        snippet = item.get("snippet", {})
        title = snippet.get("title", "Untitled")
        if vid_id:
            lines.append(f"  - {title}: https://www.youtube.com/watch?v={vid_id}")
    return "\n".join(lines)


def fetch_web_search(query: str, max_results: int = 3) -> str:
    """
    Optional web search. Uses DuckDuckGo instant answer API (no key required).
    Returns snippets or empty string on failure.
    """
    q_encoded = urllib.parse.quote(query)
    url = f"https://api.duckduckgo.com/?q={q_encoded}&format=json"
    try:
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req, timeout=8) as resp:
            data = json.loads(resp.read().decode("utf-8"))
    except (urllib.error.URLError, urllib.error.HTTPError, json.JSONDecodeError, OSError):
        return ""

    results = []
    abstract = data.get("Abstract", "").strip()
    if abstract:
        results.append(f"  - {data.get('Heading', '')}: {abstract}")
    for r in data.get("RelatedTopics", [])[:max_results]:
        if isinstance(r, dict) and r.get("Text"):
            results.append(f"  - {r['Text'][:200]}...")
        elif isinstance(r, str) and r:
            results.append(f"  - {r[:200]}...")
    if not results:
        return ""
    return "Web search results:\n" + "\n".join(results)


def fetch_recommendations(body: str) -> str:
    """
    Extract a search query from body and fetch YouTube + optional web results.
    Used when "recommend" intent matches. Returns combined string or empty.
    """
    # Heuristic: use last sentence or phrase after "for", "about", "on"
    body_lower = (body or "").lower()
    query = body.strip()[:100]  # fallback
    for sep in [" for ", " about ", " on ", " regarding "]:
        if sep in body_lower:
            idx = body_lower.rfind(sep) + len(sep)
            query = body[idx:].strip()[:80]
            break
    if not query or len(query) < 3:
        return ""

    parts = []
    yt = fetch_youtube_recommendations(query, max_results=5)
    if yt:
        parts.append(yt)
    web = fetch_web_search(query, max_results=2)
    if web:
        parts.append(web)
    return "\n\n".join(parts) if parts else ""
