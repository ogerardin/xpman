# Home Dashboard

![Main window](assets/screenshots/Screenshot%202026-09-17%20at%2023.50.17.png)
*The Home dashboard.*

## X-Plane Header

The header shows `X-Plane {version} ({variant})` for your selected installation, or **"No X-Plane installation selected"** if none is open. The folder path is a hyperlink — click it to reveal the X-Plane executable in Finder / Explorer / Files. The **Log file** hyperlink opens the X-Plane log.

## Start X-Plane

The **Start X-Plane** button launches X-Plane. It is disabled if the selected X-Plane installation is for a different platform (e.g. you opened a Windows install on macOS).

The **Install anything...** button opens the universal install wizard (see [Installing Add-ons](09-installing-addons.md)).

## X-Plane Updates Available

When a new X-Plane version is available, the dashboard shows rows for **Release** and/or **Beta** updates with a version arrow (`current → available`). Click **Run Installer** to launch the updater. A **Release notes** link is shown when available. Beta updates show a confirmation dialog with the hint: *"Check for new betas as well as updates."*

## Disk Usage

A horizontal segmented bar shows disk space consumed by: Aircraft, Global scenery, Custom scenery, Disabled scenery, and Other. Hover over a segment for details. Sizes are computed asynchronously.

## Library Tiles

Four clickable tiles let you jump to each management panel: **Aircraft**, **Scenery**, **Nav data**, **Plugins**. Each tile shows a badge with `N updates` when Skunkcrafts updates are available for that category. Disabled plugins and scenery are excluded from the update count.

## Updates Available

When enabled addons support the Skunkcrafts Updater protocol and have updates pending, they are listed here grouped by **Aircraft**, **Scenery**, and **Plugins**. Disabled plugins and scenery are omitted. Each row shows the addon name, `current → latest`, and an **Update** button that opens the Skunkcrafts update wizard (see [Updating Add-ons](10-updating-addons.md)).
