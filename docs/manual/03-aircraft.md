# Aircraft

![Aircraft management](assets/screenshots/aircraft.png)
*The Aircraft panel.*

## Toolbar

- **Reload** — Re-scan the X-Plane aircraft folder.
- **Install...** — Open the universal install wizard to install an aircraft from a `.zip` or `.7z` archive (see [Installing Add-ons](09-installing-addons.md)).
- **Search** — Filter aircraft by name, author, or studio (debounced for responsiveness). Click the **×** inside the search box to clear the search text without changing the selected **Filter**.
- **Filter** — Dropdown: `All`, `Category: …`, `Studio: …`, `Studio ≠ Laminar Research`.

## Card Grid

Each aircraft is displayed as a card with:

- **Thumbnail** image
- **Name** and **studio / author**
- **Badges**: version, **Update…** for Skunkcrafts or **Open download page** for a manual update, **Beta available** when applicable, `N liveries` (expandable to show livery mini-cards)

Hover over a card to reveal icon buttons: **Explore ACF property tree**, **Inspect**, **Reveal**, **Uninstall**.

Double-click a card to open the **Aircraft details** window — a Name/Value tree of the ACF file's properties.

## Recognized Aircraft

Any aircraft with an `.acf` file is listed. For most, XPman shows what the ACF file contains. XPman also **recognizes** a few popular aircraft and gives them extras: a reliable version number, relevant links and manuals, and sometimes a check for newer versions.

| Aircraft | What XPman adds |
|----------|-----------------|
| **ZIBO Mod B737-800X** | Version from the mod's own files; **Inspect** reports newer versions published on the ZIBO download site, and warns if the recommended AviTab and Terrain Radar plugins are missing; community, forum and download links |
| **Calibra Simulations A330-200** | Version from the aircraft's own files; warns when the installed X-Plane is older than 12.4.4 (it will probably not work until X-Plane is updated); warns when a Carda engine mod (GE or PW) is required by at least one livery but not installed (the sim shows a "CARDA MOD MISSING" box for 30s on first load — expected until the mod is copied in); website, release notes, forum, bug-report and Discord links |
| **Colimata Concorde FXP** | Version, Colimata and forum links, and the manuals bundled with the aircraft |
| **FlightFactor A350 XWB** | Version and a link to the briefing |
| **Dmax3D Eurofighter Typhoon** | Version and a link to the manufacturer's page |
| **XPFR aircraft** | Version and a link to the aircraft's page on xpfr.org |

For the Calibra A330-200, Concorde, A350 and Eurofighter, the aircraft name is fixed and can't be edited in **Properties**.

Newer versions of supported aircraft that aren't distributed through Skunkcrafts (such as ZIBO) appear in **Inspect**, on the dashboard, and as an **Open download page** action on the card. Download and install these updates manually (see [Updating Add-ons](10-updating-addons.md)).

More aircraft will be recognized in future releases. If your favorite aircraft is missing, you are welcome to [submit a pull request on GitHub](https://github.com/ogerardin/xpman) to add it.

## Context Menu

Right-click a card or livery for additional actions:

- **Reveal in Finder** / **Show in Explorer** / **Show in Files** (platform-dependent label)
- **Remove macOS quarantine** — macOS only, shown only when the aircraft folder is quarantined. Removes the macOS quarantine attribute recursively, allowing any bundled plugins to load.
- **Trim binaries to {platform}** — Removes the aircraft's bundled plugin binaries and native libraries (.dylib, .dll, .so) for other platforms; shown only when such binaries are detected. For Skunkcrafts-managed aircraft, the removed files are also added to `skunkcrafts_updater_ignore.txt` so the updater does not restore them; to restore everything, delete that file and run the updater.
- **Uninstall this variant** — Only shown when several variants of the same aircraft share the folder. Uninstalls just the selected variant (its `.acf` and icon files), leaving the folder and files shared with the other variants — including liveries — in place.
- **Uninstall aircraft** — Shows a confirmation dialog listing the aircraft that will be removed.
- **Inspect** — Opens the Inspection results window showing the outcome of all inspections.
- **Explore ACF property tree** — Opens the Aircraft details window (same as double-click).
- **Properties** — Shows aircraft properties; **Name**, **Callsign**, **Tail number**, **ICAO code**, **Description**, **Author**, **Studio**, and **Notes** can be edited and saved. The name is read-only for add-ons with a fixed display name. Closing with unsaved edits prompts to Save, Discard, or Cancel. After the dialog closes, cards refresh to show saved values.
- **Update…** — Opens the Skunkcrafts channel-review wizard (shown only when an update is available).
- **Open download page** — Opens the publisher's page when a manual update is available; it does not install the update.
- **Skunkcrafts: Locked by developer** — Shown when the developer has disabled remote updates.
- **Links** — Dynamic submenu with links from the addon's manifest.
- **Manuals** — Dynamic submenu listing PDF manuals bundled with the aircraft.
