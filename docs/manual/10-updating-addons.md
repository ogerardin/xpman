# Updating Add-ons

XPman integrates with the **Skunkcrafts Updater** protocol.
Plugins, aircraft, and scenery packages that include a `skunkcrafts_updater.cfg` file can be updated directly 
from their repositories, without requiring installation of the Skunkcraft updater application.

## Detecting Updates

- The Home dashboard shows an **Updates Available** section grouped by category.
- Each panel (Aircraft, Scenery, Plugins) shows an update marker on addons with available updates.
- Library tiles show **N updates** badges.
- Disabled plugins and scenery are excluded from update markers, dashboard counts and rows, and update actions. Aircraft are not disableable.

## Starting an Update

- Click **Update** in the Home dashboard.
- Or right-click the addon and choose **Update via Skunkcrafts Updater**.

## Skunkcrafts Update Wizard

### Page 1 — Review

*"Review update information."*

Shows:

- **Addon:** name
- **Current version:** installed version
- **Available version:** latest version (or "Could not fetch remote version")
- **Update status:** "Already up to date" or "N file(s) to update (size)"

**Next** is disabled when there is nothing to update.

### Page 2 — Updating

*"Updating..."*

- **Preparing update...** label and progress bar.

## Locked by Developer

Some addons show **Skunkcrafts: Locked by developer** instead of an update option. This means the developer has disabled remote updates for that addon. Contact the developer for updates.
