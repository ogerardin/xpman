# Scenery Classes Standalone Window — Implementation Record

**Date:** 2026-08-28
**Status:** Complete — verified with `mvn -B -DskipTests clean package` (BUILD SUCCESS)

> This plan supersedes the Preferences-dialog approach recorded in
> `2026-08-26-settings-dialog.md` for the Scenery Classes editor. The Preferences
> dialog has been removed entirely.

**Goal:** Move scenery-class editing into a standalone non-modal window opened from
the Scenery panel toolbar, and delete the Preferences dialog.

## File Map

| Action | File | Purpose |
|--------|------|---------|
| **Create** | `xpman-fx/src/main/resources/fxml/sceneryClasses.fxml` | Non-modal window: rules editor + Save/Close button bar |
| **Create** | `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/rules/SceneryClassesController.java` | Window controller (constructor-injected `XPmanFX`, `setSceneryController(...)` back-ref) |
| **Modify** | `xpman-fx/src/main/resources/fxml/panels/scenery.fxml` | Add "Scenery classes..." button (`#openSceneryClasses`, `fth-list`) |
| **Modify** | `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/SceneryController.java` | Store `mainController`, add `openSceneryClasses()` |
| **Delete** | `xpman-fx/src/main/resources/fxml/settings.fxml` | Preferences dialog removed |
| **Delete** | `xpman-fx/src/main/java/com/ogerardin/xpman/panels/settings/SettingsController.java` | Preferences controller removed (package dir also removed) |
| **Modify** | `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java` | Remove `openSettings()` + import; stale-recent-path `Files.exists` confirmation prompt in `openXPlane` |
| **Modify** | `xpman-fx/src/main/resources/fxml/main.fxml` | Remove "Preferences..." menu item, keep separators before Quit |
| **Modify** | `xpman-fx/src/main/java/module-info.java` | Remove `opens com.ogerardin.xpman.panels.settings` |
| **Modify** | `xpman-fx/src/main/resources/fxml/organize_wizard/page1.fxml` | Hint now points to the Scenery panel button |
| **Modify** | `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/wizard/Page1Controller.java` | Comment updated |

## Implementation Notes

- `SceneryClassesController` mirrors the removed `XPmanFX.openSettings()` loader
  pattern: `loader.setControllerFactory(...)` returning `new SceneryClassesController(mainController)`.
- The close button's `CANCEL_CLOSE` + `#close` handler is required because a raw
  `Stage` has no default `DialogPane` button wiring (unlike the old dialog).
- Save semantics: `rulesController.getItems()` → `setOrderedSceneryClasses(...)` →
  `config.setSceneryClasses(...)` → `saveConfig()` → `sceneryController.reload()`.
- Stale-path prompt: `Files.exists(folder)` pre-check (import `java.nio.file.Files`);
  on missing folder, confirmation alert removes the entry from `getRecentPaths()`,
  saves, refreshes the Open Recent menu, and returns. The lambda captures a local
  `String stalePath` (the `Path` is reassigned afterwards by `toRealPath()`, so it
  is not effectively final).

## Verification

```bash
mvn -B -DskipTests clean package   # BUILD SUCCESS (full reactor)
```

Suggested manual smoke test: open Scenery panel → "Scenery classes..." → edit rules →
Save → verify scenery table ranks update; Close without Save discards. Also: File >
Open Recent on a deleted folder prompts to remove the stale entry.