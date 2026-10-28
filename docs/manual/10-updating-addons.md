# Updating Add-ons

XPman integrates with the **Skunkcrafts Updater** protocol.
Plugins, aircraft, scenery packages, and Skunkcrafts-managed FlyWithLua scripts (e.g. SGES) that include a `skunkcrafts_updater.cfg` file can be updated directly 
from their repositories, without requiring installation of the Skunkcraft updater application.

Some addons additionally ship a `skunkcrafts_updater_beta.cfg` file, declaring a **beta channel** alongside the normal one.

## Detecting Updates

An update is available when the addon's files actually differ from the ones the repository publishes — XPman compares the local files' checksums against the remote channel's file list, exactly like the original Skunkcrafts Updater does. Version numbers alone never decide: an addon whose version label changed but whose files did not is reported as up to date, not as an update.

- The Home dashboard shows an **Updates Available** section grouped by category, with one row per addon showing all offered channel versions together and one **Update…** button.
- Aircraft cards and the Scenery and Plugins Version columns show the same **Update…** action. Aircraft cards additionally show **Beta available** when beta content is offered; plugin and scenery rows keep channel versions in the tooltip only.
- The **Update…** button's tooltip lists the available channels and their versions; when both are available, it lists both.
- Library tiles show **N updates** badges, counting addons (an addon with both channels pending counts once).
- Disabled plugins, scenery, and FlyWithLua scripts are excluded from update markers, dashboard counts and rows, and update actions. Aircraft are not disableable.

When both channels have files to download, click **Update…** and choose which one to install in the wizard. Home and the management panels use the same review flow.

### Beta channels

The beta channel is offered only when its file list differs from the stable one's: a beta that carries the same content as stable adds nothing and is not offered, whatever the version labels say. Switching is episodic — XPman remembers no channel preference:

- Installing a beta records the beta version in the addon's updater config, but the stable channel's address is left untouched.
- While you are on a beta, Stable is offered when its content differs from your installed files — choosing it in the wizard is the way back.
- The official Skunkcrafts Updater application keeps working on the same folder.

## Starting an Update

- Click **Update…** in a panel (Aircraft, Scenery, Plugins).
- Or click **Update…** in the Home dashboard.
- Or right-click the addon and choose **Update…** (shown only when an update is available).

Every entry point lands on a channel that has files to download: Stable when both are offered, otherwise the only offered channel, explicitly named in the review.

## Skunkcrafts Update Wizard

The wizard window is resizable, and remembers its size between sessions; it always opens centered. The bounds are kept unchanged when navigating between pages. The window has a minimum usable size, including when restoring an older saved size. On the review page, the content scrolls when space is limited so the navigation buttons remain visible.

### Page 1 — Review

*"Review update information."*

Shows the add-on name as a heading, a compact two-column summary, and the download status below a separator:

- **Heading:** add-on name
- **Installed:** installed version
- **Channel:** a Stable/Beta choice when both channels have updates, otherwise the name of the only offered channel. Stable is selected initially when both are available.
- **Available:** latest version of the selected channel (or "?" when its label could not be fetched)
- **Update status:** "N file(s) to update (size)", or "Already up to date", or "Files are up to date — local and remote versions differ" when the version label drifted without any file change

**Next** is disabled when there is nothing to update.

### Page 2 — Updating

*"Updating..."*

- **Preparing update...** label and progress bar.

## Locked by Developer

Some addons show **Skunkcrafts: Locked by developer** instead of an update option. This means the developer has disabled remote updates for that addon. Contact the developer for updates.
