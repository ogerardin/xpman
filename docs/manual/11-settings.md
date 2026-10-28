# Settings

Open Settings via **File → Settings...** (or `Cmd/Ctrl + ,`).

The Settings dialog has a category tree on the left and a settings pane on the right. Buttons: **OK** / **Cancel**.

## General

- **Confirm quit** — When enabled (default), XPman shows a **"Do you really want to quit?"** dialog on quit.
- **Ignore beta updates** — When enabled, addons' beta channels are treated as unavailable everywhere: no beta badges or tooltips, no beta-only addons in the Home updates section, and the update wizard offers the stable channel only. X-Plane's own updates are included: the Beta version is not offered on the dashboard. Takes effect immediately on OK — update displays reload in the background. Default is off.

## Tools

Embeds the tools manager (same as **Tools → Manage tools...**). Features a **Reload** toolbar, **Available** / **Installed** segmented filter, tool cards on the left, and a detail view on the right.

## Persistence

XPman saves your preferences to `~/.xpman` (JSON format). The following settings are persisted:

| Setting | Description |
|---------|-------------|
| `lastXPlanePath` | Last opened X-Plane folder (reopened at startup) |
| `recentPaths` | Recent X-Plane folders (File → Open Recent) |
| `theme` | `"dark"` (default) or `"light"` |
| `confirmQuit` | `true` (default) — show quit confirmation dialog |
| `ignoreBetaUpdates` | `false` (default) — treat beta channels as unavailable |
| `lastPosition` | Main window position and size |
| `settingsPosition` | Settings dialog position |
| `wizardSize` | Install wizard size |
| `skunkcraftsWizardSize` | Skunkcrafts update wizard size |
| `settingsCategory` | Last selected settings category |
| `sceneryClasses` | User-defined scenery organizer rules |

## Dialog Position

The Settings dialog remembers its position and the last selected category between sessions.
