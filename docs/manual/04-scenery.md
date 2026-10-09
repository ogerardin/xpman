# Scenery

![Scenery management](assets/screenshots/scenery.png)
*The Scenery panel.*

The scenery panel allows you to view sceneries and manage the entries of your `scenery_packs.ini` file. 
Control order, enable or disable them, or let the wizard re-order them.

Note: changes are never saved automatically; whenever a change is made, the message "UNSAVED CHANGES" appears, and 
you will need to press the "Save" button to save the changes to `scenery_packs.ini`.

## Toolbar

- **Reload** — Re-scan scenery packages.
- **▲ / ▼** — Move the selected scenery pack up or down one rank in `scenery_packs.ini`.
- **Install...** — Open the universal install wizard for scenery packages.
- **Scenery classes...** — Open the Scenery classes window (see [Scenery Classes](05-scenery-classes.md)).
- **Organize scenery_pack.ini** — Automatically reorder `scenery_packs.ini` by class rank (see below).
- **Open scenery_packs.ini** — Open the file in your system text editor.

## Table

The table lists all discovered scenery packages. Columns:

| Column | Description |
|--------|-------------|
| Icon | Package type indicator |
| Update marker | Shown when an enabled scenery package has a Skunkcrafts update available |
| **Rank** | Position in `scenery_packs.ini` |
| **Enabled** | Toggle switch (only for entries listed in `scenery_packs.ini`) |
| **Name** | Package name |
| **Version** | Package version |
| **Has airport?** | Pill shown if the package contains an airport |
| **Library?** | Pill shown if the package is a library |
| **Tile count** | Number of terrain tiles |
| **Obj count** | Number of OBJ objects |
| **Class** | Organizer class (e.g. Airport, Library, X-Plane Landmark); shows `Folder missing` when the directory no longer exists |

Hover over a column header for a tooltip explaining its purpose.

## Reordering

Drag rows to reorder (ini-listed entries only), or use the **▲** / **▼** toolbar buttons. A drop-target highlight shows the insertion point.

## Enable / Disable

Toggle the **Enabled** switch to include or exclude a package from `scenery_packs.ini`. Disabled entries are styled differently. Entries whose folder is missing are also visually distinct.

## Context Menu

Right-click a row for:

- **Reveal**
- **Enable Scenery Package** / **Disable Scenery Package**
- **Add to scenery_packs.ini** / **Remove from scenery_packs.ini**
- **Uninstall** — Confirmation dialog with details.
- **Inspect** — Opens Inspection results.
- **Update via Skunkcrafts Updater** — Available only for enabled packages that support updates.
- **Skunkcrafts: Locked by developer**
- **Links** — Dynamic submenu.

## Organize Workflow

1. Click **Organize scenery_pack.ini**.
2. A confirmation dialog appears: *"This will add unlisted scenery packages, remove invalid entries, and sort scenery packages by class rank. No change will be saved until you click on the 'Save' button."*
3. After organizing, the **UNSAVED CHANGES** indicator appears in the toolbar.
4. Review the new order in the table.
5. Click **Save** to persist the changes to `scenery_packs.ini`.

## Empty State

When no scenery packages are found, the panel shows **"No scenery to show"**.
