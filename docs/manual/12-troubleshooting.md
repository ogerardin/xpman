# Troubleshooting

## XPman says the X-Plane folder is invalid

Select the top-level X-Plane folder—the one containing the X-Plane executable. See [Getting started](01-getting-started.md).

## Windows blocks XPman

If SmartScreen appears when you run the installer, select **More info**, then **Run anyway**. XPman is open source; its build code is available in the [GitHub repository](https://github.com/ogerardin/xpman).

## macOS blocks XPman

If macOS prevents XPman from opening, go to **System Settings → Privacy & Security** and select **Open Anyway**.

## A plugin or aircraft doesn't load on macOS

Downloaded plugins and aircraft may carry a macOS quarantine attribute. In XPman's Plugins panel or Aircraft panel, right-click the affected item and choose **Remove macOS quarantine**.

## Find logs and report a problem

To diagnose a problem, choose **Help → Debug logging** to enable detailed logging for the current session. The setting resets the next time XPman starts.

XPman log files are stored in:

- macOS: `~/Library/Logs/XPman/`
- Windows: `%LOCALAPPDATA%\XPman\logs\`
- Linux: `$XDG_STATE_HOME/XPman/logs/`, or `~/.local/state/XPman/logs/` if `XDG_STATE_HOME` is not set

Choose **Help → Export logs** to create a ZIP archive to attach to a report. The X-Plane `Log.txt` is available from the **Log file** link on the Home dashboard.

To report a bug, choose **Help → Submit issue**. Include the steps to reproduce it, your X-Plane and XPman versions, and the exported XPman logs. Find the XPman version in **Help → About XPman**.
