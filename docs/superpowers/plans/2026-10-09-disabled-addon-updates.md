# Disabled Add-on Updates Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Exclude disabled plugins and scenery from all Skunkcrafts update signals and actions.

**Architecture:** Put update eligibility in the existing `SkunkcraftsUpdatable` domain contract. Plugins use their folder-derived enabled state; scenery packages mirror the authoritative ini disabled flag. Update availability re-checks eligibility live, while Plugin and SceneryPackage omit inspection update results only when disabled, preserving custom latest-version sources.

**Tech Stack:** Java 25, Maven, JUnit Jupiter, Hamcrest, Markdown manual.

---

### Task 1: Cover shared update eligibility and inspection behavior

**Files:**
- Modify: `xpman-api/src/test/java/com/ogerardin/xplane/test/skunkcrafts/SkunkcraftsUpdatableTest.java`
- Modify: `xpman-api/src/test/java/com/ogerardin/xplane/test/inspection/UpdateAvailableInspectionTest.java`

- [x] Add a `FakeAddon` test case where a subclass reports `isSkunkcraftsUpdatable() == false` but has a newer fetched version; assert `isSkunkcraftsUpdateAvailable()` is false.
- [x] Add an inspection case where a custom version source reports a newer `getLatestVersion()` without a Skunkcrafts config; assert the generic inspection still reports the update.
- [x] Run `mvn test -pl xpman-api -Dtest=SkunkcraftsUpdatableTest,UpdateAvailableInspectionTest`; confirm the new cases fail before implementation.

### Task 2: Gate plugin and scenery domain eligibility

**Files:**
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/skunkcrafts/SkunkcraftsUpdatable.java`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/plugins/Plugin.java`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryPackage.java`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryManager.java`
- Modify: `xpman-api/src/test/java/com/ogerardin/xplane/test/scenery/SceneryManagerSystemExclusionTest.java`

- [x] Make update availability require current eligibility:

```java
default boolean isSkunkcraftsUpdateAvailable() {
    if (!isSkunkcraftsUpdatable()) return false;
    String latest = getSkunkcraftsLatestVersion();
    return latest != null && !Objects.equals(getVersion(), latest);
}
```

- [x] In `Plugin` and `SceneryPackage`, override `isSkunkcraftsUpdatable()` to return the domain enabled state and `SkunkcraftsUpdatable.super.isSkunkcraftsUpdatable()`.
- [x] For scenery, make the package's `enabled` flag represent ini eligibility: initialize it as enabled by default, set it false in `SceneryManager.buildEntry` for a disabled ini item, and synchronize it in `updateDisabled` when the in-memory ini flag changes. Remove `isLocatedInAuthorizedBase` if it becomes unused; folder placement alone is not authoritative.
- [x] Run `mvn test -pl xpman-api -Dtest=SkunkcraftsUpdatableTest,PluginTest,SceneryManagerSystemExclusionTest`; confirm all pass.

### Task 3: Suppress update inspections for disabled plugins and scenery

**Files:**
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/plugins/Plugin.java`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryPackage.java`
- Modify: `xpman-api/src/test/java/com/ogerardin/xplane/test/plugins/PluginTest.java`
- Modify: `xpman-api/src/test/java/com/ogerardin/xplane/test/scenery/SceneryEntryTest.java`

- [x] Add plugin and scenery inspection tests with a stubbed newer latest version; assert disabled items omit the update warning.
- [x] In `Plugin.inspect` and `SceneryPackage.inspect`, append update inspection results only when `isEnabled()` is true. Keep `UpdateAvailableInspection` generic for custom-version aircraft and plugins.
- [x] Run `mvn test -pl xpman-api -Dtest=PluginTest,SceneryEntryTest,UpdateAvailableInspectionTest`; confirm all pass.

### Task 4: Update the user manual

**Files:**
- Modify: `docs/manual/02-home-dashboard.md`
- Modify: `docs/manual/04-scenery.md`
- Modify: `docs/manual/07-plugins.md`
- Modify: `docs/manual/10-updating-addons.md`

- [x] State in the update chapter that disabled plugins/scenery are excluded from update markers, dashboard counts/rows, and update actions; retain the existing aircraft behavior.
- [x] Adjust plugin and scenery marker/action descriptions to say they apply to enabled items. Clarify on the Home chapter that dashboard add-on update counts/rows omit disabled plugins/scenery.
- [x] Run `./scripts/build-manual.sh 1.0.1-SNAPSHOT`; confirm the PDF and wiki output are generated successfully.

### Task 5: Run final verification

**Files:**
- Verify all changes with Maven and inspect the final diff.

- [x] Run `mvn test -pl xpman-api`.
- [x] Run `mvn test`.
- [x] Run `git diff --check` and inspect `git diff` to confirm the change is limited to update eligibility, tests, and manual wording.
