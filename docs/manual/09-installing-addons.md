# Installing add-ons

Use the universal installer to install aircraft, scenery, plugins, navigation data, FlyWithLua scripts, and supported tools from `.zip` or `.7z` archives. XPman detects the archive contents and checks them before installing.

## Install from an archive

1. Choose **File → Universal installer**, or choose **Install** in a management panel.
2. Select the `.zip` or `.7z` archive and choose **Next**.
3. Review the inspection results. Errors block installation; warnings explain potential problems but still allow you to continue. For aircraft, XPman reports when the aircraft requires a newer X-Plane version.
4. Choose **Next** to install. When the operation completes, choose **Finish**.

If installation fails, the dialog shows the failed file and an error message. Expand the error details for technical information.

## Scenery packages

XPman adds installed scenery packages to `scenery_packs.ini` when that file exists, so they appear in the Scenery panel immediately. If X-Plane has not yet created the file, the package is installed and X-Plane registers it on its first launch. To set the load order, use **Organize scenery_pack.ini**, review the result, then choose **Save**. See [Scenery](04-scenery.md).

XPman can also unpack archives that preserve X-Plane folder structure, such as a versioned folder containing `Aircraft/...`. Check the inspection results before proceeding; XPman shows whether the archive can be installed.
