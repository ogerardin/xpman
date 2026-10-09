# Nav Data

![Nav data](assets/screenshots/navdata.png)
*The Nav data panel.*

## Toolbar

- **Reload** — Re-scan navigation data sets.
- **Install...** — Open the universal install wizard for nav data.

## Layer Cards

Navigation data is organized as a vertical stack of **layer cards**. Each card shows:

- **Status ball** — Inactive (grey), OK (green), warnings (yellow), or errors (red). Hover for details.
- **Name** — Layer name.
- **Layer i/n badge** — Position in the layer stack (e.g. `Layer 3/5`).
- **Short state** — e.g. `Cycle 2610` (the AIRAC cycle identifier).
- **Help button** (?) — Opens the Nav data info dialog.

### Nav Data Info Dialog

Shows the rendered description, folder path, AIRAC cycle, metadata, build information, and a link to the [X-Plane nav data documentation](https://developer.x-plane.com/article/navdata/).

## Override Semantics

- If a layer overrides others, it shows an **"overrides N layers"** badge.
- If a layer is overridden by another, it shows **"ignored by layer i"**.

## Files

Each card has an **"N files"** toggle. Expanding it shows per-file rows:

- Icon, file name, `absent` marker (when the file is missing from disk)
- **AIRAC …** badge
- Size and modification date

## Context Menu

Right-click a layer card for:

- **Reveal** — Show the folder in Finder / Explorer / Files.
- **Inspect** — Opens the Inspection results window.

## Empty States

- **"Loading nav data..."** — While scanning.
- **"No nav data to show"** — When no nav data sets are found.
