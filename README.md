![macOS](https://img.shields.io/badge/mac%20os-000000?style=for-the-badge&logo=macos&logoColor=white)
![Windows](https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)
![Linux](https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black)

![GitHub top language](https://img.shields.io/github/languages/top/ogerardin/xpman)
![GPL-3.0](https://img.shields.io/github/license/ogerardin/xpman)
[![Build Status](https://circleci.com/gh/ogerardin/xpman/tree/main.svg?style=shield)](https://app.circleci.com/pipelines/github/ogerardin/xpman?branch=main)
[![Build status](https://ci.appveyor.com/api/projects/ogerardin/xpman/branch/main?svg=true)](https://ci.appveyor.com/project/ogerardin/xpman/branch/main)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/fd86ae4c0e164762babd6bf8059c02e7)](https://app.codacy.com/gh/ogerardin/xpman/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Maintainability](https://qlty.sh/gh/ogerardin/projects/xpman/maintainability.svg)](https://qlty.sh/gh/ogerardin/projects/xpman)

# X-Plane Manager
X-Plane Manager (or XPman) is intended to be a configuration manager for Laminar Research's flight simulator [X-Plane](https://www.x-plane.com/) (version 11 and later).

Similar tools exist but they are not cross-platform, have limited functionality, are difficult to use or are payware.


## Goals 
- Manage X-Plane aircraft and liveries, scenery packages, nav data, plugins and extensions
- Work on all platforms supported by X-Plane (Windows, Mac, Linux)  
- Offer idiomatic installation (e.g. setup.exe on Windows) and close-to-native look and feel

## Status
Currently in development stages. Some things work, some don't... 
Check [feature status](docs/features.md).

You know the drill: USE AT YOUR OWN RISK.

# Installing X-Plane Manager
Download the package appropriate for your platform from [GitHub releases](https://github.com/ogerardin/xpman/releases) and install
it.
All packages are bundled with a Java runtime so you don't have to worry about installing Java.

## Windows
When running the EXE or MSI, SmartScreen might display a dialog saying "Windows protected your PC".
To run the installer you have to click on "More info" then "Run anyway". X-Plane Manager
doesn't contain any malware, it's 100% open source and all the code used to build and package it
is public and auditable.

# Development
X-Plane Manager is a Standalone JavaFX application built with **Java 25** and **JavaFX 25**. 
Platform installers (.dmg/.pkg on macOS, .exe/.msi on Windows, .deb/.rpm on Linux) are produced via **jpackage**, each bundled with a platform-specific JRE. 

It is split in 3 modules:
- xpman-api is a pure Java API to interact with X-Plane's installation.
- xpman-fx is the JavaFX UI built on top of xpman-api
- xpman-fx-dist is responsible for packaging xpman-fx in distributable forms



