#!/usr/bin/env python3
"""
Remove white/light border from app icon by replacing ALL light pixels
with the gradient edge color. Gradient is dark (luminance < 100).
"""
from PIL import Image
import sys

# Match the icon gradient edge (dark blue-green)
FILL_COLOR = (26, 74, 69, 255)  # #1a4a45

# Replace ONLY light gray/white - gradient/truck are dark (luminance < 150)
# Border is RGB ~230 (lum 230), white 255 - use 232 to catch border but not gradient
LUM_THRESHOLD = 232

def fix_icon(path_in: str, path_out: str):
    img = Image.open(path_in).convert("RGBA")
    pixels = img.load()
    w, h = img.size
    
    replaced = 0
    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            lum = 0.299 * r + 0.587 * g + 0.114 * b
            if lum >= LUM_THRESHOLD:
                pixels[x, y] = FILL_COLOR
                replaced += 1
    
    img.save(path_out, "PNG")
    print(f"Replaced {replaced} light pixels -> {path_out}")

if __name__ == "__main__":
    src = sys.argv[1] if len(sys.argv) > 1 else "app/src/main/res/drawable/ic_launcher_foreground_src.png"
    fix_icon(src, src)
