# Home Dashboard

![Main window](assets/screenshots/home.png)
*The Home dashboard.*

## X-Plane Header

The header shows `X-Plane {version} ({variant})` for your selected installation, or **"No X-Plane installation selected"** if none is open. The folder path is a hyperlink — click it to reveal the X-Plane executable in Finder / Explorer / Files. The **Log file** hyperlink opens the X-Plane log. **Analyze last session** reads `Log.txt` from the most recent simulator start and opens a report with session statistics (version, timings, flight, plugin counts) and grouped warnings/errors, such as plugin load failures, enabled content not loaded, and an unclean exit. It supports X-Plane 11.3x and 12 logs. Some findings can refer to X-Plane's own data, such as airport data in `apt.dat`, rather than an installed add-on. Select a row to read its full details below the table; use the severity filters to focus on errors, warnings, or information. Right-click a row to search the web for the message or copy the details. Common X-Plane messages include a plain-language explanation and occurrence count. Related adjacent lines (such as FMOD sound errors and their event descriptions) are paired and shown together. Missing plugins and scenery packs are consolidated into single rows with counts, and all detail listings include the original `Log.txt` line numbers for manual reference.

## Start X-Plane

The **Start X-Plane** button launches X-Plane. It is disabled if the selected X-Plane installation is for a different platform (e.g. you opened a Windows install on macOS).

The **Install anything...** button opens the universal install wizard (see [Installing Add-ons](09-installing-addons.md)).

## X-Plane Updates Available

When a new X-Plane version is available, the dashboard shows one **X-Plane** row with the installed version and all offered **Stable** and **Beta** versions together. Click **Update…** to review the channel before opening the installer. Stable is selected initially when both are offered; when only one is offered, its channel is named explicitly. A **Release notes** link follows the selected channel when available. When **Ignore beta updates** is enabled (see [Settings](11-settings.md)), the Beta version is not offered.

Click **Open X-Plane Installer** to continue, then manually click **Update X-Plane** in the installer. XPman does not select the installer's update channel for you:

- For **Stable**, leave **Check for new betas as well as updates** unchecked on the installer's Welcome page.
- For **Beta**, check that box. The review shows the existing installer screenshot to help you locate it; the screenshot is shown only when Beta is selected.

Cancelling the review does not open the installer or change your installation.

## Disk Usage

A horizontal segmented bar shows disk space consumed by: Aircraft, Global scenery, Custom scenery, Disabled scenery, and Other. Hover over a segment for details. Sizes are computed asynchronously.

## Library Tiles

Four clickable tiles let you jump to each management panel: **Aircraft**, **Scenery**, **Nav data**, **Plugins**. Counts reflect what X-Plane actually loads: all aircraft, enabled scenery entries, enabled plugins, and installed nav data layers. Each tile shows a badge with `N updates` when supported providers report updates for that category, including manual downloads; the badge counts addons — an addon with updates pending on both the stable and the beta channel counts once. Disabled plugins and scenery are excluded from the update count; updatable FlyWithLua scripts (e.g. SGES) count under **Plugins**.

The **Nav data** tile shows the number of layers that have data files installed and are actually read by X-Plane (layers shadowed by a sim-wide override are excluded). It also shows red and orange pills reporting the total number of error and warning messages across those active layers — e.g. a cycle mismatch between `earth_fix.dat` and `FAACIFP18`, or US-only coverage.

## Updates Available

When enabled addons have updates pending, they are listed here grouped by **Aircraft**, **Scenery**, and **Plugins**. Updatable FlyWithLua scripts (e.g. SGES) are listed under **Plugins**. Disabled plugins, scenery, and FlyWithLua scripts are omitted. Each addon appears once, with its installed version and all offered versions together (for example, `Stable 2.1 · Beta 2.2-b1`). Click **Update…** for Skunkcrafts to open the same channel-review wizard as the management panels; Stable is selected initially when both channels are offered. Publication-based updates instead show **Open download page**, which opens the publisher's site for manual installation (see [Updating Add-ons](10-updating-addons.md)).
