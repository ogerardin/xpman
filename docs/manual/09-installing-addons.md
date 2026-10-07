# Installing Add-ons

XPman can install aircraft, scenery, plugins, and tools from `.zip` or `.7z` archives via the **Universal installer**. XPman auto-detects the archive content and applies the appropriate installation logic.

## Opening the Wizard

- **File → Universal installer...**
- Or click **Install...** in any panel's toolbar.

## Page 1 — Source Archive

*"Please select source archive (.zip or .7z file)."*

- **Source file:** text field + **Browse...** button (file chooser filters for `Archives *.zip *.7z`).
- **Next** is enabled only when the file exists. If the file does not exist, the validation message **"File does not exist!"** is shown.

## Page 2 — Inspection Results

*"Please review the messages below and press 'Next' to proceed with installation."*

A table with columns:

| Column | Description |
|--------|-------------|
| **Severity** | Icon indicating severity (info, warning, error) |
| **Message** | Description of the finding |

XPman auto-detects the archive content (aircraft, scenery, plugin, or tool) and runs inspections. If any **ERROR** is present, **Next** is disabled. Warnings are non-blocking — you can proceed past them.

## Page 3 — Installing

*"Installing..."*

- Current file label and progress bar.
- **Previous** and **Finish** are disabled during installation.
- Errors are shown in an error dialog.
- On success, **"Done!"** is displayed and **Finish** becomes enabled.

## Notes

The universal installer handles all supported content types transparently. You don't need to know whether an archive contains an aircraft, scenery package, plugin, or tool — XPman detects it automatically.
