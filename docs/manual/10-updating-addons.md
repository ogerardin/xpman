# Updating Add-ons

XPman integrates with the **Skunkcrafts Updater** protocol.
Plugins, aircraft, scenery packages, and Skunkcrafts-managed FlyWithLua scripts (e.g. SGES) that include a `skunkcrafts_updater.cfg` file can be updated directly 
from their repositories, without requiring installation of the Skunkcraft updater application.

Some addons additionally ship a `skunkcrafts_updater_beta.cfg` file, declaring a **beta channel** alongside the normal one.

## Detecting Updates

An update is available when the addon's files actually differ from the ones the repository publishes — XPman compares the local files' checksums against the remote channel's file list, exactly like the original Skunkcrafts Updater does. Version numbers alone never decide: an addon whose version label changed but whose files did not is reported as up to date, not as an update.

- The Home dashboard shows an **Updates Available** section grouped by category, with one row per channel that has files to download (`Update` and, when the addon has a beta channel with distinct content, `Update Beta`).
- Each panel (Aircraft, Scenery, Plugins) shows an **Update** button in the Version column for addons with available updates.
- In the Scenery and Plugins panels, the **Update** button's tooltip lists the available channels and their versions; when both are available, it lists both.
- Library tiles show **N updates** badges, counting addons (an addon with both channels pending counts once).
- Disabled plugins, scenery, and FlyWithLua scripts are excluded from update markers, dashboard counts and rows, and update actions. Aircraft are not disableable.

When both channels have files to download, you choose which one to install — the dashboard rows and the wizard's channel selector both offer the choice.

### Beta channels

The beta channel is offered only when its file list differs from the stable one's: a beta that carries the same content as stable adds nothing and is not offered, whatever the version labels say. Switching is episodic — XPman remembers no channel preference:

- Installing a beta records the beta version in the addon's updater config, but the stable channel's address is left untouched.
- While you are on a beta, the stable row reappears as soon as stable publishes different content — installing it is the way back.
- The official Skunkcrafts Updater application keeps working on the same folder.

## Starting an Update

- Click **Update** in a panel (Aircraft, Scenery, Plugins).
- Or click **Update** / **Update Beta** in the Home dashboard.
- Or right-click the addon and choose **Update via Skunkcrafts Updater** (shown only when an update is available).

Every entry point lands on a channel that has files to download: the wizard opens on the channel you clicked, or on the only channel with an update.

## Skunkcrafts Update Wizard

The wizard window is resizable, and remembers its size between sessions; it always opens centered. The bounds are kept unchanged when navigating between pages.

### Page 1 — Review

*"Review update information."*

Shows:

- **Addon:** name
- **Current version:** installed version
- **Channel:** a Stable/Beta choice when both channels have updates, otherwise the name of the only offered channel. Stable is selected initially when both are available.
- **Available version:** latest version of the selected channel (or "?" when its label could not be fetched)
- **Update status:** "N file(s) to update (size)", or "Already up to date", or "Files are up to date — local and remote versions differ" when the version label drifted without any file change

**Next** is disabled when there is nothing to update.

### Page 2 — Updating

*"Updating..."*

- **Preparing update...** label and progress bar.

## Locked by Developer

Some addons show **Skunkcrafts: Locked by developer** instead of an update option. This means the developer has disabled remote updates for that addon. Contact the developer for updates.
