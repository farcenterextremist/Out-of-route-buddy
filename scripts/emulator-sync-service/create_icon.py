"""
Generate a stylized icon.ico for the OutOfRouteBuddy Emulator Sync desktop shortcut.
Requires: pip install Pillow
Run from this folder: python create_icon.py
Then re-run create_desktop_shortcut.ps1 to refresh the shortcut icon.
"""
try:
    from PIL import Image, ImageDraw
except ImportError:
    print("Pillow is required. Run: pip install Pillow")
    raise SystemExit(1)

import os

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
OUT_PATH = os.path.join(SCRIPT_DIR, "icon.ico")

# Colors (OutOfRouteBuddy-style dark theme)
BG = (0x1E, 0x1E, 0x1E, 255)
FG = (0xBB, 0x86, 0xFC, 255)  # accent
WHITE = (255, 255, 255, 255)

def draw_icon(size):
    img = Image.new("RGBA", (size, size), BG)
    d = ImageDraw.Draw(img)
    # Circular "sync" arrows: two arcs suggesting refresh/sync
    margin = max(2, size // 6)
    box = [margin, margin, size - margin, size - margin]
    w = max(1, size // 12)
    d.arc(box, 45, 270, fill=FG, width=w)
    d.arc(box, 225, 450, fill=WHITE, width=w)
    return img

def main():
    # Draw at largest size; Pillow will downscale for ICO
    img = draw_icon(48)
    img.save(OUT_PATH, format="ICO", sizes=[(16, 16), (32, 32), (48, 48)])
    print("Created:", OUT_PATH)
    print("Re-run create_desktop_shortcut.ps1 to refresh the shortcut icon.")

if __name__ == "__main__":
    main()
