#!/usr/bin/env python3
"""
Boost color saturation of the launcher foreground icon (gradient + truck).
Writes in place. Black/dark pixels are preserved; gradient becomes more vivid.
"""
from PIL import Image, ImageEnhance
import os

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    root = os.path.normpath(os.path.join(script_dir, ".."))
    path = os.path.join(root, "app", "src", "main", "res", "drawable", "ic_launcher_foreground_src.png")
    if not os.path.isfile(path):
        print(f"Not found: {path}")
        return
    img = Image.open(path).convert("RGBA")
    # Enhance color (saturation): 1.0 = no change, 2.0 = much more saturated
    saturated = ImageEnhance.Color(img).enhance(2.0)
    saturated.save(path, "PNG")
    print(f"Boosted saturation -> {path}")

if __name__ == "__main__":
    main()
