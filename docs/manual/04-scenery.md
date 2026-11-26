# Scenery

![Scenery panel showing package load order and enabled status](assets/screenshots/scenery.png)
*The Scenery panel.*

The Scenery panel manages scenery packages and their entries in `scenery_packs.ini`. Choose **Reload** to rescan packages. Changes to the file are not saved automatically.

## Change scenery load order

Drag a row or select it and use **▲** or **▼** to change its rank. Choose **Organize scenery_pack.ini** to add unlisted packages, remove invalid entries, and sort packages by their scenery class. Review the proposed changes, then choose **Save** to write them to `scenery_packs.ini`. Until you save, the **UNSAVED CHANGES** indicator is shown.

To customize how automatic ordering works, choose **Scenery classes**. See [Scenery classes](05-scenery-classes.md).

## Enable or remove a scenery entry

Use the **Enabled** switch to disable or enable a package in `scenery_packs.ini`. Disabling keeps the entry but tells X-Plane not to load it. Choose **Save** to keep the change.

To remove an entry from `scenery_packs.ini`, right-click it and choose **Remove from scenery_packs.ini**. This removes the entry, not the package files; choose **Save** to write the change. If the folder remains in `Custom Scenery`, X-Plane adds the package back to `scenery_packs.ini` the next time it starts. To delete the package files, choose **Uninstall** and review the confirmation.

## Install and review scenery

Choose **Install** to install a scenery package from an archive. Installed packages are added to the scenery list; use **Organize scenery_pack.ini** to sort them. See [Installing add-ons](09-installing-addons.md).

The table shows each package's rank, enabled status, version, scenery class, and useful content indicators such as whether it contains an airport or library. Use **Inspect** in the context menu to review findings. Choose **Open scenery_packs.ini** to open the file in your text editor.
