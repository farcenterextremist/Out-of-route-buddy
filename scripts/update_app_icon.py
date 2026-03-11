#!/usr/bin/env python3
"""
Update app icon to white truck on black background.
- Converts user image (white truck on black) to adaptive icon foreground:
  black pixels -> transparent, white pixels -> opaque white
- Foreground saved to drawable/ic_launcher_foreground_src.png
- Background is set via ic_launcher_background.xml (manual edit to solid black)
"""
from PIL import Image
import os
import sys

# Adaptive icon foreground: 432x432 is xxxhdpi equivalent, scales well
FOREGROUND_SIZE = 432
# Safe zone: 72dp of 108dp - content here won't be clipped by circular masks.
# Scale truck to ~80% of canvas so edges stay inside the circle.
SAFE_ZONE_SCALE = 0.80
# Slight left offset (pixels) for visual balance
LEFT_OFFSET = 8

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    root = os.path.normpath(os.path.join(script_dir, ".."))

    # User's image path - try multiple locations
    candidates = [
        sys.argv[1] if len(sys.argv) > 1 else None,
        os.path.join(root, "assets",
            "c__Users_brand_AppData_Roaming_Cursor_User_workspaceStorage_e6fb439657fb579e7a91a02de2e35926_images_Gemini_Generated_Image_akeh34akeh34akeh-4156aa54-ac8c-453c-9b56-d73b5612cfae.png"),
        r"C:\Users\brand\.cursor\projects\c-Users-brand-OutofRoutebuddy\assets\c__Users_brand_AppData_Roaming_Cursor_User_workspaceStorage_e6fb439657fb579e7a91a02de2e35926_images_Gemini_Generated_Image_akeh34akeh34akeh-4156aa54-ac8c-453c-9b56-d73b5612cfae.png",
    ]
    src_path = None
    for p in candidates:
        if p and os.path.isfile(p):
            src_path = p
            break
    if not src_path:
        print("ERROR: Source image not found. Pass path as argument or place in assets/")
        return 1

    out_path = os.path.join(root, "app", "src", "main", "res", "drawable", "ic_launcher_foreground_src.png")

    img = Image.open(src_path).convert("RGBA")
    pixels = img.load()
    w, h = img.size

    # Black (or near-black) -> transparent; white (or near-white) -> opaque white
    LUM_BLACK = 30   # below this = background, make transparent
    LUM_WHITE = 225  # above this = truck, keep white
    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            lum = 0.299 * r + 0.587 * g + 0.114 * b
            if lum <= LUM_BLACK:
                pixels[x, y] = (0, 0, 0, 0)  # transparent
            elif lum >= LUM_WHITE:
                pixels[x, y] = (255, 255, 255, 255)  # opaque white
            else:
                # Anti-aliased edge: blend
                t = (lum - LUM_BLACK) / (LUM_WHITE - LUM_BLACK) if LUM_WHITE > LUM_BLACK else 1
                t = max(0, min(1, t))
                alpha = int(255 * t)
                pixels[x, y] = (255, 255, 255, alpha)

    # Shrink to fit within safe zone so circular icon masks don't clip the truck
    inner_size = int(FOREGROUND_SIZE * SAFE_ZONE_SCALE)
    w, h = img.size
    scale = inner_size / max(w, h)
    new_w = int(w * scale)
    new_h = int(h * scale)
    img = img.resize((new_w, new_h), Image.Resampling.LANCZOS)

    # Center on transparent canvas, slightly shifted left
    canvas = Image.new("RGBA", (FOREGROUND_SIZE, FOREGROUND_SIZE), (0, 0, 0, 0))
    paste_x = max(0, (FOREGROUND_SIZE - new_w) // 2 - LEFT_OFFSET)
    paste_y = (FOREGROUND_SIZE - new_h) // 2
    canvas.paste(img, (paste_x, paste_y))
    canvas.save(out_path, "PNG")
    print(f"Foreground saved -> {out_path}")
    return 0

if __name__ == "__main__":
    sys.exit(main())
