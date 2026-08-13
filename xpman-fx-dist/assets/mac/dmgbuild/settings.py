# dmgbuild settings for the X-Plane Manager DMG.
# Reproduces the previous jpackage DMG customization pixel-perfect
# (layout values read from the DMG's .DS_Store; see build-dmg.sh).

import os.path

APP_NAME = defines["APP_NAME"]
SCRIPT_DIR = defines["SCRIPT_DIR"]

# Same compression as jpackage's default DMG (zlib)
format = "UDZO"

files = [APP_NAME + ".app"]
symlinks = {"Applications": "/Applications"}
hide_extensions = [APP_NAME + ".app"]

# Background: byte-identical to the jpackage default (multi-frame TIFF).
# dmgbuild copies it as-is to .background/background.tiff (no @2x sibling, or
# it would try to combine frames with tiffutil).
background = os.path.join(SCRIPT_DIR, "background.tiff")

# Volume icon: byte-identical to the jpackage default.
icon = os.path.join(SCRIPT_DIR, "VolumeIcon.icns")

# Window bounds / view options (match the current DMG's .DS_Store)
window_rect = ((10, 1020), (540, 360))
icon_size = 128.0
text_size = 16.0
arrange_by = None
show_icon_preview = True
show_item_info = False
label_pos = "bottom"
show_status_bar = False
show_tab_view = False
show_toolbar = False
show_pathbar = False
show_sidebar = False
grid_offset = (0, 0)
grid_spacing = 100.0

# Icon positions: exact Iloc values from the current DMG's .DS_Store
# (dmgbuild writes icon_locations verbatim as Iloc entries).
icon_locations = {
    APP_NAME + ".app": (115, 116),
    "Applications": (423, 116),
}
