# Installing Add-ons

XPman can install aircraft, scenery, plugins, and tools from `.zip` or `.7z` archives via the **Universal installer**. XPman auto-detects the archive content and applies the appropriate installation logic.

## Opening the Wizard

- **File → Universal installer...**
- Or click **Install...** in any panel's toolbar.

The wizard window is resizable, and remembers its size between sessions; it always opens centered. The bounds are kept unchanged when navigating between pages.

## Page 1 — Source Archive

*"Please select source archive (.zip or .7z file)."*

> XPman will automatically detect the archive contents (aircraft, scenery, plugin, nav data, or tool) and let you review what will be installed before anything is written.

- **Source file:** text field + **Browse...** button (file chooser filters for `Archives *.zip *.7z`).
- **Next** is enabled only when the file exists. If the file does not exist, the validation message **"File does not exist!"** is shown.

## Page 2 — Inspection Results

*"Please review the messages below and press 'Next' to proceed with installation."*

A table with columns:

| Column | Description |
|--------|-------------|
| **Severity** | Icon indicating severity (info, warning, error) |
| **Message** | Description of the finding |
| **Details** | ⓘ icon where present; hover to see additional details in a tooltip |

XPman auto-detects the archive content (aircraft, scenery, plugin, or tool) and runs inspections. If any **ERROR** is present, **Next** is disabled. Warnings are non-blocking — you can proceed past them.

For aircraft, XPman also compares the archive's ACF spec version with your X-Plane version: an aircraft built for a **newer** X-Plane than yours is reported as an error (**Next** is disabled — it will not work), while an aircraft built for an **older** X-Plane only produces a warning you can proceed past (an updated, native build of that aircraft may be available).

## Page 3 — Installing

*"Installing..."*

- Current file label and progress bar.
- **Previous** and **Finish** are disabled during installation.
- On failure, the page header and file label show the failure reason; an error dialog also provides technical details.
- On success, **"Done!"** is displayed and **Finish** becomes enabled.

## Notes

The universal installer handles all supported content types transparently. You don't need to know whether an archive contains an aircraft, scenery package, plugin, or tool — XPman detects it automatically.

**Scenery packages and libraries** are automatically registered at the bottom of `scenery_packs.ini` upon installation, so they immediately appear as enabled, ranked entries in the Scenery panel instead of unlisted folders. (X-Plane itself also appends missing Custom Scenery folders to the ini on its next launch — the registration just keeps XPman's view in sync right away.) If `scenery_packs.ini` doesn't exist yet (X-Plane has never launched with this profile), the package is still installed but not registered — X-Plane will register it on first launch. You can use the **Organize** button in the Scenery panel to reorder packages by class (airports, landmarks, overlays, libraries, etc.).

Archives that mirror the X-Plane folder structure (a versioned folder containing `Aircraft/…`) are installed with their internal structure preserved. For example, an archive containing `Aircruiser XP12 v1_3/Aircraft/Hangar 23/Aircruiser/…` is extracted so the aircraft lands at `Aircraft/Hangar 23/Aircruiser/` — matching the publisher's intended layout — rather than nesting under the versioned wrapper.

Archives must contain a single root folder, but may also include root-level files (manuals, readmes, license files) alongside it. These root-level files are ignored during installation — only the contents of the root folder are extracted.
