# Troubleshooting

## Invalid X-Plane Folder

If you select a folder that is not a valid X-Plane installation, XPman shows the error **"Invalid X-Plane folder"**. Ensure you select the top-level X-Plane directory — the one containing the X-Plane executable.

## Windows SmartScreen

When running the EXE or MSI installer, Windows SmartScreen may display a warning. Click **More info** then **Run anyway** — XPman is 100% open source and all build code is public and auditable.

See [Installation](00-introduction.md#windows) for details.

## macOS Gatekeeper

macOS may block the app because it is not signed with an Apple Developer ID. Go to **System Settings > Privacy & Security** and click **Open Anyway** to allow it to run.

See [Installation](00-introduction.md#macos) for details.

## macOS Quarantine on Plugins

Some plugins downloaded from the internet are quarantined by macOS and will not load. Right-click the plugin in the Plugins panel and choose **Remove macOS quarantine** to allow it to run.

## Submitting Issues

If you encounter a bug or unexpected behavior, use **Help → Submit issue…** to open the GitHub issues page. Please include:

- Your X-Plane version
- XPman version (from **Help → About XPman**)
- Steps to reproduce the issue

## Logs

XPman logs are written to the console. Use **Help → ? Help** to open the Wiki (this manual). The X-Plane log is accessible via the **Log file** hyperlink on the Home dashboard.

## About XPman

**Help → About XPman** shows:

- Version: `X-Plane Manager {version}`
- Build: `{number} (commit #{hash} on {branch})`
- Runtime: `{java} {version} {arch}`
- VM: `{vm} by {vendor}`
- CPU: `{type} ({n} cores)`
- Link to the GitHub repository
- License: GPLv3
