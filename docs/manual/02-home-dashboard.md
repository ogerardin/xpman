# Home dashboard

The Home dashboard summarizes the selected X-Plane installation, available updates, disk usage, and navigation-data status.

## Open or start X-Plane

The header shows the selected installation and its version. Choose the folder link to reveal the X-Plane folder in your file manager, or **Log file** to open `Log.txt`. Choose **Start X-Plane** to launch the simulator. This button is unavailable when the selected installation is for a different platform.

Choose **Analyze last session** to review the most recent X-Plane log. The report groups common warnings and errors, includes their line numbers, and lets you filter by severity or inspect details. Some findings concern X-Plane data rather than add-ons.

Choose **Install anything** to install an add-on from an archive. See [Installing add-ons](09-installing-addons.md).

## Update X-Plane

When an update is available, the dashboard shows the Stable and, when offered, Beta versions. Choose **Update** to review the channel and release notes before opening the X-Plane installer.

XPman opens the installer but does not choose its update channel. On the installer's Welcome page:

- For **Stable**, leave **Check for new betas as well as updates** clear.
- For **Beta**, select that checkbox.

Reviewing or cancelling the update does not change your installation. To hide Beta offers for X-Plane and add-ons, enable **Ignore beta updates** in [Settings](11-settings.md).

## Review add-on updates

The **Updates Available** section groups enabled add-ons by category. Choose **Update** to review and install a Skunkcrafts update in XPman, or **Open download page** to install a publisher's update manually. The download-page action does not install the update; reload the relevant panel after installing it. See [Updating add-ons](10-updating-addons.md).

The category tiles open their management panels and show update counts when available. Disabled plugins, scenery, and FlyWithLua scripts are excluded. An add-on with updates on both channels counts once; FlyWithLua script updates appear under **Plugins**.

The **Nav data** tile counts layers with data files that X-Plane currently reads. It also shows the number of errors and warnings reported for those layers. See [Nav data](06-navdata.md).

## Check disk usage

The segmented bar shows disk usage for aircraft, global scenery, custom scenery, disabled scenery, and other files. Hold the pointer over a segment for details. XPman calculates sizes in the background.
