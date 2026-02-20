# Toolbar background image

The app toolbar uses a **cracked road texture** as its background (instead of plain black).

## Required image file

Place your cracked-road image in this folder with this **exact** name:

- **`toolbar_bg_cracked_road_img.png`**

The image will be stretched to fill the toolbar. Use a high-enough resolution so it still looks good when scaled (e.g. 1080px wide or larger).

If this file is missing, the app will **fail to build** with a resource-not-found error. To revert to the plain black toolbar, remove the `setBackgroundResource` call in `TripInputFragment.setupUI()` and delete `toolbar_background_cracked_road.xml` from this folder.
