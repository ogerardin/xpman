# Aircraft

![Aircraft management](assets/screenshots/Screenshot%202026-09-17%20at%2023.51.12.png)
*The Aircraft panel.*

## Toolbar

- **Reload** — Re-scan the X-Plane aircraft folder.
- **Install...** — Open the universal install wizard to install an aircraft from a `.zip` or `.7z` archive (see [Installing Add-ons](09-installing-addons.md)).
- **Search** — Filter aircraft by name, author, or studio (debounced for responsiveness).
- **Filter** — Dropdown: `All`, `Category: …`, `Studio: …`, `Studio ≠ Laminar Research`.

## Card Grid

Each aircraft is displayed as a card with:

- **Thumbnail** image
- **Name** and **studio / author**
- **Badges**: version, `Update available` (when a Skunkcrafts update exists), `N liveries` (expandable to show livery mini-cards)

Hover over a card to reveal icon buttons: **Explore properties**, **Inspect**, **Reveal**, **Uninstall**.

Double-click a card to open the **Aircraft details** window — a Name/Value tree of the ACF file's properties.

## Context Menu

Right-click a card or livery for additional actions:

- **Reveal in Finder** / **Show in Explorer** / **Show in Files** (platform-dependent label)
- **Uninstall aircraft** — Shows a confirmation dialog listing the aircraft that will be removed.
- **Inspect** — Opens the Inspection results window showing the outcome of all inspections.
- **Explore properties** — Opens the Aircraft details window (same as double-click).
- **Update via Skunkcrafts Updater** — Opens the Skunkcrafts update wizard (when applicable).
- **Skunkcrafts: Locked by developer** — Shown when the developer has disabled remote updates.
- **Links** — Dynamic submenu with links from the addon's manifest.
- **Manuals** — Dynamic submenu listing PDF manuals bundled with the aircraft.

