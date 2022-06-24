[![Linux](https://svgshare.com/i/Zhy.svg)](https://svgshare.com/i/Zhy.svg)
[![macOS](https://svgshare.com/i/ZjP.svg)](https://svgshare.com/i/ZjP.svg)
[![Windows](https://svgshare.com/i/ZhY.svg)](https://svgshare.com/i/ZhY.svg)
![GitHub top language](https://img.shields.io/github/languages/top/ogerardin/xpman)
![GPL-3.0](https://img.shields.io/github/license/ogerardin/xpman)

[![Build Status](https://circleci.com/gh/ogerardin/xpman/tree/java11.svg?style=shield)](https://app.circleci.com/pipelines/github/ogerardin/xpman?branch=java11)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/fd86ae4c0e164762babd6bf8059c02e7)](https://www.codacy.com/gh/ogerardin/xpman/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ogerardin/xpman&amp;utm_campaign=Badge_Grade)
[![Maintainability](https://api.codeclimate.com/v1/badges/5844bbd3cdb4db2c2f7b/maintainability)](https://codeclimate.com/github/ogerardin/xpman/maintainability)

# X-Plane Manager
X-Plane Manager (or XPman) is intended to be a configuration manager for Laminar Research's wonderful flight simulator X-Plane.

Such tools exist but they are not cross-platform, have limited functionality, are difficult to use or are payware.


## Goals 
- Manage X-Plane aircrafts, scenery, plugins and extensions
- Work on all platforms supported by X-Plane (Windows, Mac, Linux)  
- Offer idiomatic installation (e.g. setup.exe on Windows) and close-to-native look and feel

## Status
Currently in development stages. Some things work, some don't... 
Check [feature status](https://github.com/ogerardin/xpman/blob/main/features.md).

USE AT YOUR OWN RISK! At this point I can't guarantee that it won't erase the whole X-Plane directory or set fire to your house.

# Installing X-Plane Manager
Download the package appropriate for your platform from [GitHub releases](https://github.com/ogerardin/xpman/releases) and install
it.
All packages are bundled with a JRE so you don't have to worry about installing Java.

## Windows
When running the EXE or MSI, SmartScreen might display a dialog saying "Windows protected your PC".
To run the installer you have to click on "More info" then "Run anyway". X-Plane Manager
doesn't contain any malware, it's 100% open source and all the code used to build and package it
is public and auditable.

## Development
X-Plane Manager is a Standalone JavaFX application. The "main" branch is now using Java 17. 

It is split in 3 modules:
- xpman-api is a pure Java API to interact with X-Plane's installation.
- xpman-fx is the JavaFX UI built on top of xpman-api
- xpman-fx-dist is responsible for packaging xpman-fx in distributable forms



