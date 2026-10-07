# Introduction

XPman is a cross-platform configuration manager for [X-Plane](https://www.x-plane.com/) 11 and 12. It lets you browse, install, update, and 
organize your aircraft, scenery, plugins, and tools from a single desktop application.

![Main window](assets/screenshots/Screenshot%202026-09-17%20at%2023.50.17.png)
*The XPman main window.*

## Features

- **Aircraft** — Install, delete, and manage aircraft and liveries. Recognize specific aircraft models for version checking and update notifications.
- **Scenery** — Install scenery packages and auto-order `scenery_packs.ini` for optimal load order.
- **Plugins** — Install, delete, and manage global plugins. System plugins are protected from deletion. Plugin versions extracted from native binaries. PDF manuals supported.
- **FlyWithLua** — Manage FlyWithLua scripts with SGES integration.
- **Nav data** — List and manage navigation data sets, including XP12 airspace and ATC data.
- **Tools** — Install and uninstall generic tools from manifest files, with custom install directory support.
- **X-Plane updates** — Get notified when a new X-Plane version is available and start the updater.
- **Disk usage** — View disk space consumed by each category (aircraft, scenery, plugins, etc.).
- **Skunkcrafts Updater integration** — Detect and apply updates for addons that support the Skunkcrafts Updater protocol.

## Installation

Download the latest release for your platform from [GitHub Releases](https://github.com/ogerardin/xpman/releases). All packages are bundled with a Java runtime — no separate Java installation required.

| Platform | Format |
|----------|--------|
| macOS    | `.dmg` or `.pkg` |
| Windows  | `.exe` or `.msi` |
| Linux    | `.deb` or `.rpm` |

### Windows

When running the EXE or MSI, Windows SmartScreen may display a warning. Click **More info** then **Run anyway** — XPman is 100% open source and all build code is public and auditable.

### macOS

macOS may block the app because it is not signed with an Apple Developer ID. Go to **System Settings > Privacy & Security** and click **Open Anyway** to allow it to run.
