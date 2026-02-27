# Toolbar background – cracked road texture

The app toolbar uses a **cracked road texture** as its background (instead of plain black/gradient).

## Current implementation

The toolbar uses an image asset in the drawable folder:

- **`drawable/toolbar_bg_cracked_road_img.png`** – Cracked road texture image (600×112px base; stretches to fill)
- **`drawable/toolbar_background_cracked_road.xml`** – Bitmap drawable referencing the image
- **`drawable-night/toolbar_background_cracked_road.xml`** – Same image for dark mode

Applied in `custom_toolbar.xml` via `android:background="@drawable/toolbar_background_cracked_road"`.

## Replacing with custom image

To use your own texture, replace `toolbar_bg_cracked_road_img.png` in `app/src/main/res/drawable/` with your image. Use a high resolution (e.g. 1080px wide or larger) for sharp scaling.

## Reverting to plain/gradient toolbar

To revert to the previous gradient, in `custom_toolbar.xml` change `android:background="@drawable/toolbar_background_cracked_road"` to `android:background="@drawable/gradient_grey_appbar"`.
