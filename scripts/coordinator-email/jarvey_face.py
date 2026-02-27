#!/usr/bin/env python3
"""
Jarvey's ASCII face — larger, more beautiful, optionally animated.
Defaults to pure ASCII for maximum terminal compatibility.
Optional: ANSI color + Unicode box-drawing if your terminal supports it.
"""

from __future__ import annotations

import os
import sys
import time

# ─── Theme + styling ─────────────────────────────────────────────────────────

CSI = "\033["
RESET = f"{CSI}0m"


def _supports_unicode(stream=None) -> bool:
    """Check if output encoding likely supports UTF-8 box-drawing."""
    stream = stream or sys.stderr
    try:
        enc = stream.encoding or sys.stderr.encoding or sys.stdout.encoding or ""
        return "utf" in (enc or "").lower()
    except Exception:
        return False


def _is_truthy_env(name: str) -> bool:
    return os.environ.get(name, "").strip().lower() in ("1", "true", "yes", "y", "on")


def _is_falsy_env(name: str) -> bool:
    return os.environ.get(name, "").strip().lower() in ("0", "false", "no", "n", "off")


def _supports_ansi(stream=None) -> bool:
    """
    Best-effort: only emit ANSI when stream is a TTY and NO_COLOR isn't set.
    """
    stream = stream or sys.stderr
    if os.environ.get("NO_COLOR"):
        return False
    try:
        return bool(getattr(stream, "isatty", lambda: False)())
    except Exception:
        return False


def _use_color(stream=None, force: bool | None = None) -> bool:
    """
    Determine whether to colorize output.
    - If force is set, obey it.
    - Else obey env JARVEY_FACE_COLOR (1/0/auto).
    - Else default to TTY-only.
    """
    stream = stream or sys.stderr
    if force is not None:
        return force
    if _is_truthy_env("JARVEY_FACE_COLOR"):
        return True
    if _is_falsy_env("JARVEY_FACE_COLOR"):
        return False
    return _supports_ansi(stream=stream)


def _color(code: str, s: str, enabled: bool) -> str:
    if not enabled:
        return s
    return f"{CSI}{code}m{s}{RESET}"


def _pad(s: str, width: int) -> str:
    s = (s or "")[:width]
    return s + (" " * max(0, width - len(s)))


def _normalize_rect(lines: list[str]) -> list[str]:
    w = max(len(l) for l in lines) if lines else 0
    return [l + (" " * (w - len(l))) for l in lines]


def _get_theme(stream=None, theme: str | None = None) -> str:
    """
    theme: 'ascii' | 'unicode' | 'auto'
    Env override: JARVEY_FACE_THEME
    """
    stream = stream or sys.stderr
    t = (theme or os.environ.get("JARVEY_FACE_THEME", "auto")).strip().lower()
    if t not in ("ascii", "unicode", "auto"):
        t = "auto"
    if t == "auto":
        return "unicode" if _supports_unicode(stream=stream) else "ascii"
    return t


# ─── Face rendering ──────────────────────────────────────────────────────────

def _render_ascii(eyes: str = "oo", status: str = "Email Ready", color: bool = False) -> str:
    """
    Pure ASCII "deluxe" face: framed, shaded, readable in 80-col terminals.
    """
    outer_c = "36"      # cyan
    inner_c = "34"      # blue
    dim_c = "90"        # gray
    eye_c = "32;1"      # bright green
    badge_c = "33;1"    # bright yellow
    bright_c = "37;1"   # bright white

    status = _pad(status, 18)
    e = (eyes or "oo")[:2].ljust(2)

    OUT_W = 54
    IN_W = OUT_W - 2
    PANEL_W = 46
    PANEL_IN_W = PANEL_W - 2
    M = (IN_W - PANEL_W) // 2

    def seg(parts: list[tuple[str | None, str]]) -> str:
        return "".join(_color(code, text, color) if code else text for code, text in parts)

    def top_or_bottom() -> str:
        return seg([(outer_c, "+" + ("=" * (OUT_W - 2)) + "+")])

    def outer_line(panel_parts: list[tuple[str | None, str]]) -> str:
        # Outer cyan bars, interior as given (must be width IN_W)
        return seg([(outer_c, "|"), (None, seg(panel_parts)), (outer_c, "|")])

    def panel_border(top: bool) -> list[tuple[str | None, str]]:
        left, right = (".", ".") if top else ("'", "'")
        border_txt = left + ("-" * (PANEL_W - 2)) + right
        return [
            (None, " " * M),
            (inner_c, border_txt),
            (None, " " * (IN_W - M - PANEL_W)),
        ]

    def panel_line(text: str, *, status_line: bool = False) -> list[tuple[str | None, str]]:
        text = _pad(text, PANEL_IN_W)
        parts: list[tuple[str | None, str]] = [(None, " " * M), (inner_c, "|")]

        if status_line:
            # Build: "  [  JARVEY  ]  STATUS: " + status (padded)
            prefix = "  "
            badge_txt = "[  JARVEY  ]"
            mid = "  STATUS: "
            remainder = PANEL_IN_W - len(prefix) - len(badge_txt) - len(mid) - len(status)
            parts.extend(
                [
                    (dim_c, prefix),
                    (badge_c, badge_txt),
                    (dim_c, mid),
                    (bright_c, status),
                    (dim_c, " " * max(0, remainder)),
                ]
            )
        else:
            parts.append((dim_c, text))

        parts.append((inner_c, "|"))
        parts.append((None, " " * (IN_W - M - PANEL_W)))
        return parts

    # Build lines (uncolored structure; color applied in seg())
    lines: list[str] = []
    lines.append(top_or_bottom())
    lines.append(outer_line(panel_border(top=True)))
    lines.append(outer_line(panel_line("      .--.                .--.      ")))

    # Eyes line: build as dim text, then replace eye centers after full line assembly
    eyes_plain = _pad(f"     / ({e[0]}) \\              / ({e[1]}) \\     ", PANEL_IN_W)
    eyes_parts = [(None, " " * M), (inner_c, "|"), (dim_c, eyes_plain), (inner_c, "|"), (None, " " * (IN_W - M - PANEL_W))]
    eyes_line = seg([(outer_c, "|"), (None, seg(eyes_parts)), (outer_c, "|")])
    eyes_line = eyes_line.replace(f"({e[0]})", "(" + _color(eye_c, e[0], color) + ")", 1)
    eyes_line = eyes_line.replace(f"({e[1]})", "(" + _color(eye_c, e[1], color) + ")", 1)
    lines.append(eyes_line)

    lines.append(outer_line(panel_line("     \\  --  /              \\  --  /     ")))
    lines.append(outer_line(panel_line("      '--'      .-\"\"\"\"-.      '--'      ")))
    lines.append(outer_line(panel_line("               /  .--.  \\               ")))
    lines.append(outer_line(panel_line("          .---'  /____\\  '---.          ")))
    lines.append(outer_line(panel_line("         /      /\\____/\\      \\         ")))
    lines.append(outer_line(panel_line("         \\_____/  \\__/  \\_____/         ")))
    lines.append(outer_line(panel_line("                .-====-.                ")))

    # Status line
    # Note: we re-use panel_line with status_line=True for proper segment coloring
    status_parts = panel_line("", status_line=True)
    lines.append(seg([(outer_c, "|"), (None, seg(status_parts)), (outer_c, "|")]))

    lines.append(outer_line(panel_border(top=False)))
    lines.append(top_or_bottom())

    # Ensure rectangular output in case of ANSI wrappers
    return "\n".join(_normalize_rect(lines))


def _render_unicode(eyes: str = "●●", status: str = "Email Ready", color: bool = False) -> str:
    """
    Unicode variant: box drawing + slightly cleaner geometry.
    """
    outer_c = "36"      # cyan
    inner_c = "34"      # blue
    dim_c = "90"        # gray
    eye_c = "32;1"      # bright green
    badge_c = "33;1"    # bright yellow
    bright_c = "37;1"   # bright white

    status = _pad(status, 18)
    e = (eyes or "●●")[:2].ljust(2)

    OUT_W = 54
    IN_W = OUT_W - 2
    PANEL_W = 46
    PANEL_IN_W = PANEL_W - 2
    M = (IN_W - PANEL_W) // 2

    def seg(parts: list[tuple[str | None, str]]) -> str:
        return "".join(_color(code, text, color) if code else text for code, text in parts)

    def top() -> str:
        return seg([(outer_c, "╔" + ("═" * (OUT_W - 2)) + "╗")])

    def bottom() -> str:
        return seg([(outer_c, "╚" + ("═" * (OUT_W - 2)) + "╝")])

    def outer_line(panel_parts: list[tuple[str | None, str]]) -> str:
        return seg([(outer_c, "║"), (None, seg(panel_parts)), (outer_c, "║")])

    def panel_border(top_edge: bool) -> list[tuple[str | None, str]]:
        left, right = ("╭", "╮") if top_edge else ("╰", "╯")
        border_txt = left + ("─" * (PANEL_W - 2)) + right
        return [
            (None, " " * M),
            (inner_c, border_txt),
            (None, " " * (IN_W - M - PANEL_W)),
        ]

    def panel_line(text: str) -> list[tuple[str | None, str]]:
        text = _pad(text, PANEL_IN_W)
        return [
            (None, " " * M),
            (inner_c, "│"),
            (dim_c, text),
            (inner_c, "│"),
            (None, " " * (IN_W - M - PANEL_W)),
        ]

    lines: list[str] = []
    lines.append(top())
    lines.append(outer_line(panel_border(top_edge=True)))
    lines.append(outer_line(panel_line("      ╭──╮                ╭──╮      ")))

    eye_plain = _pad(f"     ╭╯ {e[0]}╰╮              ╭╯ {e[1]}╰╮     ", PANEL_IN_W)
    eye_parts = [(None, " " * M), (inner_c, "│"), (dim_c, eye_plain), (inner_c, "│"), (None, " " * (IN_W - M - PANEL_W))]
    eye_line = seg([(outer_c, "║"), (None, seg(eye_parts)), (outer_c, "║")])
    eye_line = eye_line.replace(e[0], _color(eye_c, e[0], color), 1)
    eye_line = eye_line.replace(e[1], _color(eye_c, e[1], color), 1)
    lines.append(eye_line)

    lines.append(outer_line(panel_line("     ╰╮  ──  ╭╯              ╰╮  ──  ╭╯     ")))
    lines.append(outer_line(panel_line("      ╰──╯      ╭────────╮      ╰──╯      ")))
    lines.append(outer_line(panel_line("               ╭╯  ╭──╮  ╰╮               ")))
    lines.append(outer_line(panel_line("          ╭────╯   ╰──╯   ╰────╮          ")))
    lines.append(outer_line(panel_line("          ╰────╮   ╭──╮   ╭────╯          ")))
    lines.append(outer_line(panel_line("               ╰╮  ╰──╯  ╭╯               ")))
    lines.append(outer_line(panel_line("                ╭╯ ╭══╮ ╰╮                ")))

    # Status line with explicit segment coloring
    prefix = "  "
    badge_txt = "【  JARVEY  】"
    mid = "  STATUS: "
    remainder = PANEL_IN_W - len(prefix) - len(badge_txt) - len(mid) - len(status)
    status_parts: list[tuple[str | None, str]] = [
        (None, " " * M),
        (inner_c, "│"),
        (dim_c, prefix),
        (badge_c, badge_txt),
        (dim_c, mid),
        (bright_c, status),
        (dim_c, " " * max(0, remainder)),
        (inner_c, "│"),
        (None, " " * (IN_W - M - PANEL_W)),
    ]
    lines.append(seg([(outer_c, "║"), (None, seg(status_parts)), (outer_c, "║")]))

    lines.append(outer_line(panel_border(top_edge=False)))
    lines.append(bottom())
    return "\n".join(_normalize_rect(lines))


def get_face(
    use_unicode: bool | None = None,
    stream=None,
    color: bool | None = None,
    status: str = "Email Ready",
    eyes_open: str | None = None,
) -> str:
    """
    Backwards-compatible entry point for Jarvey face.
    - use_unicode: True/False/None (auto)
    - color: True/False/None (auto)
    """
    stream = stream or sys.stderr
    theme = "unicode" if (use_unicode is True) else ("ascii" if (use_unicode is False) else _get_theme(stream=stream))
    use_color = _use_color(stream=stream, force=color)
    if theme == "unicode":
        return _render_unicode(eyes=eyes_open or "●●", status=status, color=use_color)
    return _render_ascii(eyes=eyes_open or "oo", status=status, color=use_color)


def print_face(use_unicode: bool | None = None, stream=None, color: bool | None = None, status: str = "Email Ready"):
    """Print the static Jarvey face to stderr (or given stream)."""
    stream = stream or sys.stderr
    print(get_face(use_unicode=use_unicode, stream=stream, color=color, status=status), file=stream)


# ─── Animation ──────────────────────────────────────────────────────────────

def _ansi_cursor_up(n: int) -> str:
    return f"\033[{n}A" if n > 0 else ""


def _ansi_clear_line() -> str:
    return "\033[2K"


def _line_count(s: str) -> int:
    return len(s.splitlines())


def _redraw_in_place(stream, old_lines: int, new_text: str):
    up = _ansi_cursor_up(old_lines) if old_lines > 0 else ""
    clear = "\n".join([_ansi_clear_line() for _ in range(old_lines)])
    stream.write(up + clear)
    stream.write(new_text)
    stream.flush()


def animate_blink(
    stream=None,
    num_blinks: int = 2,
    frame_delay: float = 0.15,
    use_unicode: bool | None = None,
    color: bool | None = None,
    status: str = "Email Ready",
):
    """
    Animate a blink sequence. Prints face, then blinks num_blinks times.
    Uses ANSI escape codes to redraw in place.
    """
    stream = stream or sys.stderr
    face_open = get_face(use_unicode=use_unicode, stream=stream, color=color, status=status, eyes_open=None)
    # "blink": replace the eye glyphs in a theme-aware way
    theme = "unicode" if (use_unicode is True) else ("ascii" if (use_unicode is False) else _get_theme(stream=stream))
    eyes_closed = "──" if theme == "unicode" else "--"
    face_blink = get_face(use_unicode=use_unicode, stream=stream, color=color, status=status, eyes_open=eyes_closed)
    lines = _line_count(face_open)

    print(face_open, file=stream)
    stream.flush()

    for _ in range(num_blinks):
        time.sleep(frame_delay * 4)  # eyes open
        _redraw_in_place(stream, lines, face_blink)
        time.sleep(frame_delay)  # eyes closed
        _redraw_in_place(stream, lines, face_open)

    stream.write("\n")
    stream.flush()


def animate_startup(
    stream=None,
    duration: float = 1.5,
    use_unicode: bool | None = None,
    color: bool | None = None,
):
    """
    Play a short startup animation:
    - face appears
    - blink
    - brief status scan/progress
    """
    stream = stream or sys.stderr
    # Respect env if caller didn't specify
    if color is None:
        color = _use_color(stream=stream, force=None)

    face_ready = get_face(use_unicode=use_unicode, stream=stream, color=color, status="Email Ready")
    lines = _line_count(face_ready)
    print(face_ready, file=stream)
    stream.flush()

    # blink
    time.sleep(max(0.05, duration * 0.25))
    theme = "unicode" if (use_unicode is True) else ("ascii" if (use_unicode is False) else _get_theme(stream=stream))
    eyes_closed = "──" if theme == "unicode" else "--"
    face_blink = get_face(use_unicode=use_unicode, stream=stream, color=color, status="Email Ready", eyes_open=eyes_closed)
    _redraw_in_place(stream, lines, face_blink)
    time.sleep(0.11)
    _redraw_in_place(stream, lines, face_ready)

    # status scan (short, tasteful)
    scan_frames = [
        "Booting  [=         ]",
        "Booting  [==        ]",
        "Booting  [===       ]",
        "Booting  [====      ]",
        "Booting  [=====     ]",
        "Booting  [======    ]",
        "Booting  [=======   ]",
        "Booting  [========  ]",
        "Booting  [========= ]",
        "Booting  [==========]",
        "Email Ready",
    ]
    frame_delay = max(0.03, min(0.09, duration / max(1, len(scan_frames)) / 1.2))
    for st in scan_frames:
        face = get_face(use_unicode=use_unicode, stream=stream, color=color, status=st)
        _redraw_in_place(stream, lines, face)
        time.sleep(frame_delay)

    stream.write("\n")
    stream.flush()


if __name__ == "__main__":
    import argparse
    p = argparse.ArgumentParser(description="Display Jarvey's face")
    p.add_argument("--theme", choices=["auto", "ascii", "unicode"], default="auto", help="Face theme (default auto)")
    p.add_argument("--unicode", action="store_true", help="Alias for --theme unicode")
    p.add_argument("--ascii", action="store_true", help="Alias for --theme ascii")
    p.add_argument("--color", action="store_true", help="Force ANSI color")
    p.add_argument("--no-color", action="store_true", help="Disable ANSI color")
    p.add_argument("--status", default="Email Ready", help="Status line text")
    p.add_argument("--animate-startup", action="store_true", help="Run startup animation (blink + scan)")
    p.add_argument("--animate-blink", action="store_true", help="Run blink animation")
    p.add_argument("--blinks", type=int, default=2, help="Number of blinks (default 2)")
    args = p.parse_args()

    if args.unicode:
        args.theme = "unicode"
    if args.ascii:
        args.theme = "ascii"

    use_unicode = True if args.theme == "unicode" else (False if args.theme == "ascii" else None)
    color = True if args.color else (False if args.no_color else None)

    if args.animate_startup:
        animate_startup(duration=1.2, use_unicode=use_unicode, color=color)
    elif args.animate_blink:
        animate_blink(num_blinks=args.blinks, use_unicode=use_unicode, color=color, status=args.status)
    else:
        print_face(use_unicode=use_unicode, color=color, status=args.status)
