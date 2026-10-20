# Plugins

![Plugin management](assets/screenshots/plugins.png)
*The Plugins panel.*

## Toolbar

- **Reload** — Re-scan plugins.
- **Install...** — Open the universal install wizard for plugins.

## Tree Table

Plugins are displayed as a tree. System plugins show a system icon. Columns:

| Column | Description |
|--------|-------------|
| System icon | Shown for built-in/system plugins |
| **Enabled** | Toggle switch |
| **Name** | Plugin name |
| **Description** | Short description |
| **Version** | Installed version (from the native binary, or the script header for FlyWithLua scripts); an **Update available** pill appears when a Skunkcrafts update is available and shows the latest version on hover |

### FlyWithLua

The FlyWithLua plugin node expands to show its Lua scripts, plus groups **Disabled scripts (n)** and **Quarantined scripts (n)**.

**SGES updates:** The SGES archive ships a Skunkcrafts updater config next to its `Scripts/` folder; XPman installs it into the `FlyWithLua` folder, which is the addon base folder the Skunkcrafts protocol expects. SGES is therefore the updatable addon: updates appear on the **SGES script row** (right-click → **Update via Skunkcrafts Updater**), not on the FlyWithLua plugin row. Uninstalling SGES removes the config.

### SimLoad Manager

SimLoad Manager (v5+) is a standalone X-Plane plugin that simulates realistic passenger, cargo, and fuel loading operations with SimBrief integration.

**Requirements:**
- X-Plane 12.4.4 or later
- SGES (optional) — for ground vehicle operations
- FlyWithLua (optional) — only required if using SGES
- Linux: `libcurl.so.4` for SimBrief integration

**Installation:**
Extract the archive and place the entire `SimLoadManager/` folder into `Resources/plugins/` (do not rename). The plugin will appear in the X-Plane menu under Plugins → SimLoad Manager. XPman enforces the minimum X-Plane version requirement (12.4.4) and will block installation if your X-Plane version is older.

**Migration from v4 (FlyWithLua script):**
No manual steps required. SimLoad Manager v5 automatically migrates v4 settings and moves the old script from `FlyWithLua/Scripts/` to `FlyWithLua/Scripts (disabled)/`.

**Platform Notes:**
- **macOS:** The plugin is not notarized by Apple. Use **Remove macOS quarantine** from the context menu if blocked.
- **Linux:** SimBrief integration requires `libcurl.so.4` (install via package manager, e.g., `sudo apt install libcurl4`).

**Automatic Updates:**
SimLoad Manager supports Skunkcrafts Updater. Once installed, future versions can be updated directly via the **Update via Skunkcrafts Updater** context menu option.

## Plugin Context Menu

Right-click a plugin for:

- **Reveal**
- **Remove macOS quarantine** — Shown only on macOS when the plugin is quarantined.
- **Enable plugin** / **Disable plugin**
- **Uninstall plugin** — Confirmation dialog with details.
- **Inspect** — Opens Inspection results.
- **Properties** — Shows a read-only Properties window with all object properties.
- **Update via Skunkcrafts Updater** — Available only for enabled plugins that have an update available.
- **Skunkcrafts: Locked by developer**
- **Links** — Dynamic submenu.
- **Manuals** — Dynamic submenu with PDF manuals.

## Script Context Menu (FlyWithLua)

Right-click a FlyWithLua script for:

- **Enable script** / **Disable script**
- **Quarantine script** / **Unquarantine script**
- **Reveal**
- **Open in text editor**
- **Manuals** — Dynamic submenu.
- **Links** — Dynamic submenu.
- **Properties** — Shows a read-only Properties window with all object properties.
- **Uninstall script** — Confirmation dialog.
- **Update via Skunkcrafts Updater** — Available only for enabled scripts that have an update available (e.g. SGES).
- **Skunkcrafts: Locked by developer** — Shown when the developer has disabled remote updates.

## Empty States

- **"Loading plugins..."** — While scanning.
- **"No plugins to show"** — When no plugins are found.
