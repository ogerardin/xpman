# Plugins

![Plugin management](assets/screenshots/Screenshot%202026-09-17%20at%2023.53.36.png)
*The Plugins panel.*

## Toolbar

- **Reload** — Re-scan plugins.
- **Install...** — Open the universal install wizard for plugins.

## Tree Table

Plugins are displayed as a tree. System plugins show a system icon. Columns:

| Column | Description |
|--------|-------------|
| System icon | Shown for built-in/system plugins |
| Update marker | Shown when a Skunkcrafts update is available |
| **Enabled** | Toggle switch |
| **Name** | Plugin name |
| **Description** | Short description |
| **Version** | Installed version (extracted from native binary) |
| **Latest version** | Latest available version |

### FlyWithLua

The FlyWithLua plugin node expands to show its Lua scripts, plus groups **Disabled scripts (n)** and **Quarantined scripts (n)**.

## Plugin Context Menu

Right-click a plugin for:

- **Reveal**
- **Remove macOS quarantine** — Shown only on macOS when the plugin is quarantined.
- **Enable plugin** / **Disable plugin**
- **Uninstall plugin** — Confirmation dialog with details.
- **Inspect** — Opens Inspection results.
- **Update via Skunkcrafts Updater** / **Skunkcrafts: Locked by developer**
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
- **Uninstall script** — Confirmation dialog.

## Empty States

- **"Loading plugins..."** — While scanning.
- **"No plugins to show"** — When no plugins are found.
