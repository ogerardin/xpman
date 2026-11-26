# Updating add-ons

XPman checks supported update providers and shows the available action. The action depends on how the publisher distributes updates:

- **Update** — Review and install a Skunkcrafts update in XPman.
- **Open download page** — Open the publisher's page to download and install the update yourself. Reload the management panel after installing it.

You can find these actions on the Home dashboard and, where available, on aircraft cards, scenery rows, and plugin or script rows. Disabled plugins, scenery, and FlyWithLua scripts are excluded from update offers.

## Install a Skunkcrafts update

1. Choose **Update** on the dashboard or in the relevant management panel.
2. Review the offered channel and files. When Stable and Beta are both available, Stable is selected initially.
3. Choose **Next** to download and install the update, then close the wizard when it finishes.

XPman checks whether the add-on's files differ from the publisher's files; a changed version label alone doesn't necessarily mean there is an update. Failed checks appear in **Inspect** rather than being reported as up to date.

### Stable and Beta channels

Beta is offered only when its files differ from Stable. XPman does not remember a channel preference: if you install Beta, choose Stable in the wizard later to return to Stable when its files differ from your installed files. The official Skunkcrafts Updater can still be used on the same add-on.

To hide Beta offers, enable **Ignore beta updates** in [Settings](11-settings.md). This also hides Beta updates for X-Plane itself.

## Install a manual update

Choose **Open download page** to visit the publisher's page, then follow its instructions to download and install the update. This action does not download or install files in XPman. Reload the management panel after installing.

## Developer-locked add-ons

**Skunkcrafts: Locked by developer** means the add-on's developer has disabled remote updates. Contact the developer for update information.
