![macOS](https://img.shields.io/badge/mac%20os-000000?style=flat-square&logo=macos&logoColor=white)
![Windows](https://img.shields.io/badge/Windows-0078D6?style=flat-square&logo=windows&logoColor=white)
![Linux](https://img.shields.io/badge/Linux-FCC624?style=flat-square&logo=linux&logoColor=black)
[![Build Status](https://github.com/ogerardin/xpman/actions/workflows/build.yml/badge.svg)](https://github.com/ogerardin/xpman/actions/workflows/build.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Maintainability](https://qlty.sh/gh/ogerardin/projects/xpman/maintainability.svg)](https://qlty.sh/gh/ogerardin/projects/xpman)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/fd86ae4c0e164762babd6bf8059c02e7)](https://app.codacy.com/gh/ogerardin/xpman/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

# XPman — X-Plane Manager

A cross-platform configuration manager for [X-Plane](https://www.x-plane.com/) 11 and 12.

## Features

- **Aircraft** — Install, delete, and manage aircraft and liveries. Recognize specific aircraft models for version checking and update notifications.
- **Scenery** — Install scenery packages and auto-order `scenery_packs.ini` for optimal load order.
- **Plugins** — Install and manage global plugins.
- **Nav data** — List and manage navigation data sets.
- **Tools** — Install and uninstall generic tools from manifest files.
- **X-Plane updates** — Get notified when a new X-Plane version is available and start the updater.
- **Disk usage** — View disk space consumed by each category (aircraft, scenery, plugins, etc.).

## Screenshots

![Main window](assets/screenshots/Screenshot%202026-08-18%20at%2001.32.55.png)

![Aircraft tree](assets/screenshots/Screenshot%202026-08-18%20at%2001.33.32.png)

![Inspection results](assets/screenshots/Screenshot%202026-08-18%20at%2001.33.42.png)

![Scenery management](assets/screenshots/Screenshot%202026-08-18%20at%2001.33.48.png)

## Installation

Download the latest release for your platform from [GitHub Releases](https://github.com/ogerardin/xpman/releases). All packages are bundled with a Java runtime — no separate Java installation required.

| Platform | Format |
|----------|--------|
| macOS | `.dmg` or `.pkg` |
| Windows | `.exe` or `.msi` |
| Linux | `.deb` or `.rpm` |

### Windows

When running the EXE or MSI, Windows SmartScreen may display a warning. Click **More info** then **Run anyway** — XPman is 100% open source and all build code is public and auditable.

### macOS

macOS may block the app because it is not signed with an Apple Developer ID. Go to **System Settings > Privacy & Security** and click **Open Anyway** to allow it to run.

## Development

XPman is a standalone JavaFX desktop application.

**Tech stack:** Java 25, JavaFX 25, Maven, Lombok, Gson, JUnit 5

### Modules

| Module | Purpose |
|--------|---------|
| `xpman-api` | Pure Java API — domain model, file parsers, inspection framework, install logic |
| `xpman-fx` | JavaFX UI — FXML views, controllers, wizards, custom cell factories |
| `xpman-fx-dist` | Distribution packaging — uber-jar repackaged into platform installers via jpackage |

### Building

Requires JDK 25.

```bash
# Full build (all modules, no tests)
mvn -B -DskipTests clean package

# Run tests
mvn test
```

See [AGENTS.md](AGENTS.md) for detailed architecture, CI/CD, and testing information.

## License

XPman is licensed under the [GNU General Public License v3.0](LICENSE).
