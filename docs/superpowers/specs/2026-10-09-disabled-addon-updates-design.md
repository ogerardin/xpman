# Disabled Add-on Updates — Design

## Goal

Disabled plugins and scenery must not participate in XPman's Skunkcrafts update feature. They should not be offered for update, marked as having an update, counted on the Home dashboard, or produce an update-available inspection warning.

## Semantics

- A plugin is disabled when `Plugin.isEnabled()` is false (its folder is under `Resources/plugins (disabled)/`).
- Scenery's `SCENERY_PACK_DISABLED` ini flag is authoritative. Legacy folder placement alone does not mean disabled, matching `SceneryEntryTest.iniEnabledPackageInDisabledFolderShouldBeEnabled`.
- Aircraft have no enabled/disabled state. FlyWithLua scripts have no Skunkcrafts update action.
- The informational “Skunkcrafts: Locked by developer” context-menu item remains available; it explains the publisher's lock, not an update action.

## Approach

Keep the behavior in the domain model so every consumer observes the same eligibility. `Plugin` and `SceneryPackage` override `isSkunkcraftsUpdatable()` to require their enabled state. For scenery, `SceneryManager` mirrors the authoritative ini flag into the package's formerly write-only `enabled` property, both when building entries and when the flag changes in memory. `SkunkcraftsUpdatable.isSkunkcraftsUpdateAvailable()` checks eligibility live, and Plugin/SceneryPackage omit inspection update results when disabled. `UpdateAvailableInspection` remains generic because aircraft and some plugins can supply a custom version source without a Skunkcrafts config.

This automatically gates context-menu actions, row/card markers, Home dashboard badges and update rows, and inspection warnings without adding UI-specific checks.

## Testing and documentation

Add focused tests for live eligibility on the shared interface, disabled plugins, scenery ini state, and inspections. Update the Home, scenery, plugins, and add-on update manual chapters to explain that disabled plugins/scenery are excluded from update detection and actions. Verify with the XPman API tests, full Maven test suite, and manual PDF build.
