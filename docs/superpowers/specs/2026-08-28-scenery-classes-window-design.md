# Scenery Classes Standalone Window — Design

**Date:** 2026-08-28
**Status:** Implemented & verified (`mvn -B -DskipTests clean package` succeeds)

## Goal

Replace the "Scenery Classes" editor that lived inside the Preferences dialog with a
standalone, non-modal window opened from a new button in the Scenery panel toolbar,
and remove the entire Preferences dialog (theme toggle and recent-paths management it
housed are relocated as described below).

## Motivations

- Preferences was a modal dialog reached via `File > Preferences...`, an awkward
  place to edit scenery-package classification rules that belong conceptually to the
  Scenery panel.
- Non-modal editing + explicit Save/Close is a better fit for a rule table the user
  may want to consult while working in the main window.

## Decisions

| Decision | Choice |
|---|---|
| Placement | New non-modal `Stage`, owner = main window, opened from a "Scenery classes...` button in the Scenery toolbar (icon `fth-list`), after "Organize scenery_pack.ini" |
| Persistence | Explicit **Save** button (`save` → `setOrderedSceneryClasses` + config update + `saveConfig()` + `SceneryController.reload()`); **Close** discards |
| Editor reuse | Existing `fxml/rules.fxml` + `RulesController` reused unchanged via `<fx:include>`; `setItems`/`getItems` API |
| Window controller | NEW `SceneryClassesController` (`@RequiredArgsConstructor(XPmanFX)`, constructor-injected via `FXMLLoader.setControllerFactory`, mirroring the removed `XPmanFX.openSettings()` pattern) |
| Dependencies | Injected programmatically (controller factory for `XPmanFX`; `setSceneryController(...)` after load for `SceneryController`, mirroring `UiAircraft.details()`/`setAircraft`) |
| Preferences dialog | **Deleted** (`settings.fxml`, `SettingsController`, `File > Preferences...` menu item, `XPmanFX.openSettings()`). Theme toggle already exists in sidebar footer; recent-paths management becomes an inline prompt on stale paths |
| Recent paths | On `openXPlane(File)`, if the folder no longer exists, show a confirmation `Alert` offering to remove it from the recent-paths list (covers the Open Recent menu and startup auto-open of a stale `lastXPlanePath`); `Files.exists()` pre-check replaces the previous reliance on `toRealPath()` throwing |
| Cross-references | Wizard `page1.fxml` hint + `Page1Controller` comment updated to point at the new window |
| Typo fix | Write "scenery" (not the old "scenary" typo) in the new window's label |
| Naming | `fxml/sceneryClasses.fxml` + `com.ogerardin.xpman.panels.scenery.rules.SceneryClassesController` (alongside `RulesController`) |

## Window Layout

`BorderPane` (pref 600x450, min 400x300), automatically themed via the global
user-agent stylesheet (`ThemeManager`):

- **center**: `VBox` — explanatory `Label` + `<fx:include fx:id="rules" source="rules.fxml" VBox.vgrow="ALWAYS"/>`
- **bottom**: `ButtonBar` — **Save** (`ButtonBar.buttonData="OK_DONE"`, `#save`) and **Close** (`fx:id="closeButton"`, `#close`, `ButtonBar.buttonData="CANCEL_CLOSE"`)

## Data Flow

- `initialize()` seeds `rulesController.setItems(new ArrayList<>(organizer.getOrderedSceneryClasses()))` — a defensive copy so edits only reach the shared organizer on Save.
- `save()` writes the editor's items back to `SceneryOrganizer`, persists via `XPManPrefs.setSceneryClasses(...)` + `saveConfig()`, then `reload()`s the Scenery panel so classifications/ranks update immediately.
- The window is intentionally non-modal; `RulesController` is a pure editor and its existing up/down/add/delete/restore-defaults behavior is untouched.

## Files Changed

| Action | File |
|---|---|
| **Create** | `xpman-fx/src/main/resources/fxml/sceneryClasses.fxml` |
| **Create** | `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/rules/SceneryClassesController.java` |
| **Modify** | `xpman-fx/src/main/resources/fxml/panels/scenery.fxml` (add button) |
| **Modify** | `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/SceneryController.java` (store `mainController`, add `openSceneryClasses()`) |
| **Delete** | `xpman-fx/src/main/resources/fxml/settings.fxml`, `com/ogerardin/xpman/panels/settings/SettingsController.java` |
| **Modify** | `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java` (remove `openSettings()`; add stale-recent-path `Files.exists` prompt) |
| **Modify** | `xpman-fx/src/main/resources/fxml/main.fxml` (remove Preferences item) |
| **Modify** | `xpman-fx/src/main/java/module-info.java` (remove `opens panels.settings`) |
| **Modify** | `xpman-fx/src/main/resources/fxml/organize_wizard/page1.fxml` + `Page1Controller.java` (stale references) |

## Verification

- `mvn -B -DskipTests clean package` succeeds (full reactor, as the CI build; no lint/typecheck step, no tests reference the settings screen).