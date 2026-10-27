# Plugins

![Plugin management](assets/screenshots/plugins.png)
*The Plugins panel.*

The Plugins panel lists everything installed in `Resources/plugins`, lets you enable, disable and uninstall plugins, and shows when updates are available.

## Toolbar

- **Reload** — Re-scan plugins.
- **Install...** — Open the universal install wizard (see [Installing Add-ons](09-installing-addons.md)).

## Plugin List

Plugins are displayed as a tree. Columns:

| Column | Description |
|--------|-------------|
| Icon | Shield for built-in system plugins (they can be neither disabled nor uninstalled); code symbol for FlyWithLua scripts |
| **Enabled** | Toggle switch. A disabled plugin is moved out of the active plugins folder, so X-Plane no longer loads it |
| **Name** | Plugin name |
| **Description** | Short description |
| **Version** | Installed version. **Update…** appears when content is available; hover for offered channel versions. **Beta available** marks beta content |

The **Update…** button opens the Skunkcrafts channel-review wizard (see [Updating Add-ons](10-updating-addons.md)). It is available for plugins or FlyWithLua scripts with pending Skunkcrafts content. Disabled plugins and disabled or quarantined scripts are excluded from update markers and cannot be updated.

## Recognized Plugins

Any plugin with an `.xpl` file is listed, even if XPman does not know it. Unrecognized plugins are named after their folder, with no description. XPman **recognizes** a few popular ones and gives them a proper name, description, links, tailored install and uninstall checks, and — for some — automatic detection of newer versions.

| Plugin | Recognized by | What XPman adds |
|---|---|---|
| FlyWithLua | `FlyWithLua` folder | Lists its scripts; uninstall warning; Linux library check on install; X-Plane.org and GitHub links; **Inspect** reports newer versions |
| SGES | Its `.lua` file | Version read from the script; Skunkcrafts **Update…** button on the script row; uninstall removes data, docs and updater files; manuals and X-Plane.org link |
| SimLoad Manager | `SimLoadManager` folder (v5 plugin) or `SimLoadManager.lua` (v4 script) | Name, description and links; X-Plane 12.4.4 check and Linux `libcurl` warning on install; v4 uninstall also removes `SLM-Data` |
| AutoGate | `AutoGate` folder | Description and X-Plane.org link; **Inspect** reports newer versions |
| AviTab | `Avitab.xpl` | Description and X-Plane.org, GitHub and Wiki links; **Inspect** reports newer versions |
| Terrain Radar | `TerrainRadar` folder | Version read from its readme; X-Plane.org link; **Inspect** reports newer versions |
| X-IvAp | `X-IvAp-64.xpl` | Links to the IVAO home page and manual; **Inspect** reports newer versions from the IVAO site |
| PluginAdmin | Folder name | System plugin: can be neither disabled nor uninstalled |
| openSAM | `openSAM` folder | Installs the plugin **and** the `openSAM_Library` scenery from the same archive, with scenery ordering; version/flavor checks and conflict warnings on install; uninstall removes both halves |

More plugins will be recognized in future releases. If your favorite plugin is missing, you are welcome to [submit a pull request on GitHub](https://github.com/ogerardin/xpman) to add it.

For plugins that report newer versions via **Inspect**, the comparison runs against the plugin's publication channel (X-Plane.org, or the IVAO site for X-IvAp). The **Update…** button itself only appears when Skunkcrafts updates are supported.

## FlyWithLua

The FlyWithLua node expands to show its Lua scripts, plus two groups: **Disabled scripts (n)** and **Quarantined scripts (n)**. Scripts are hidden while FlyWithLua itself is disabled.

Each script can be enabled, disabled or quarantined individually. Quarantining moves the script to a separate `Scripts (Quarantine)` folder, next to the regular `Scripts` and `Scripts (disabled)` folders.

Installing any FlyWithLua script (including SGES and the v4 SimLoad Manager script) is blocked if FlyWithLua is missing or disabled. On Linux, installing the FlyWithLua plugin itself warns when `libglut` or `libopenal` is missing.

Updates for scripts such as SGES appear on the **script's own row**, not on the FlyWithLua row.

### SGES

Simple Ground Equipment & Services ships a Skunkcrafts updater config; XPman installs it into the `FlyWithLua` folder, which is the addon base folder the protocol expects. SGES is therefore the updatable addon: the **Update…** button appears on the **SGES script row**, not on the FlyWithLua plugin row. Uninstalling SGES removes the config as well as its data and documentation folders.

### SimLoad Manager

SimLoad Manager is recognized in two forms:

- **v5+** — a standalone plugin installed in `Resources/plugins/SimLoadManager`.
- **v4** — a FlyWithLua script (`SimLoadManager.lua`) in `FlyWithLua/Scripts`.

The install wizard detects the archive format automatically. XPman enforces X-Plane 12.4.4 or later for the v5 plugin and blocks installation on older versions. On Linux, it warns when `libcurl.so.4` is missing (needed for SimBrief).

Uninstalling the v4 script also removes the `SLM-Data` folder next to it.

## openSAM

openSAM is recognized in two archive flavors: **v5+** (X-Plane 12 only) and **v4 and earlier** (X-Plane 11 and 12 — XPman blocks installing the wrong XP11/XP12 flavor when it can tell them apart).

Its archive contains the plugin and the `openSAM_Library` scenery; the install wizard installs both, positions the library correctly in `scenery_packs.ini`, and warns about conflicting plugins (SAM, the SAM Seasons emulator, and AutoDGS on v5+).

Uninstalling openSAM from the Plugins panel removes the plugin and the `openSAM_Library` scenery; uninstalling the library from the Scenery panel removes just the library.

## Uninstall

Uninstalling a plugin moves its entire folder to the trash. For FlyWithLua, the confirmation dialog warns that all its scripts will also be deleted; for openSAM, that the `openSAM_Library` scenery and its `scenery_packs.ini` entry will also be removed.

## Context Menus

### Plugins

Right-click a plugin for:

- **Reveal** — Show the plugin folder in your file manager.
- **Remove macOS quarantine** — macOS only, shown only when the plugin is quarantined.
- **Enable plugin** / **Disable plugin**
- **Uninstall plugin** — Confirmation dialog with plugin-specific details.
- **Inspect** — Show inspection results.
- **Properties** — Read-only view of all plugin properties.
- **Update…** — Shown only for enabled plugins with an update available.
- **Skunkcrafts: Locked by developer** — Shown when the developer has disabled remote updates.
- **Links** and **Manuals** — Submenus, shown when available.

### Scripts (FlyWithLua)

Right-click a script for:

- **Enable script** / **Disable script**
- **Quarantine script** / **Unquarantine script**
- **Reveal**
- **Open in text editor**
- **Uninstall script** — Confirmation dialog.
- **Properties**
- **Update…** / **Skunkcrafts: Locked by developer** — For scripts that support it (e.g. SGES).
- **Links** and **Manuals** — Submenus, shown when available.
