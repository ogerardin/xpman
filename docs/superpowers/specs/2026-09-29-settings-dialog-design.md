# Settings Dialog Design

**Date:** 2026-09-29
**Status:** Approved

## Goal

Add an IntelliJ-style Settings dialog to XPman: hierarchic categories on the left, settings pane on the right, with OK/Cancel semantics. Initial categories:

- **General** — a "confirm quit" checkbox preference
- **Tools** — the existing tools pane, moved out of the sidebar

## Decisions

- **Dialog type:** modal custom Stage (sceneryClasses idiom)
- **Tools entry points:** sidebar TOOLS section removed; File > "Manage tools..." kept as shortcut that opens Settings pre-selected on Tools
- **`confirmQuit` default:** `true` (no behavior change for existing users)
- **Buttons:** OK / Cancel (General prefs apply on OK, discarded on Cancel; Tools pane actions are immediate and unaffected)

## Architecture

Mirrors the existing `Section`/sidebar idiom:

- **`SettingsCategory` enum** — label + Ikonli icon literal + FXML path, same shape as `Section`
- **`SettingsController`** — builds a TreeView from the enum, lazily loads/caches category panes exactly like `XPmanFX.showSection`/`sectionCache`, calls `apply()` on every loaded `SettingsPage` controller on OK
- **`SettingsPage` interface** — 3-line contract (`void apply()`) for panes with editable state; `GeneralController` implements it; `ToolsController` does not (its actions are immediate)
- **`confirmQuit` preference** — `boolean confirmQuit = true` on `JfxAppPrefs` (base class, since `JfxApp.quit()` is generic over `C extends JfxAppPrefs` and already has `getConfig()`); one `if`-gate in `JfxApp.quit()` covers both menu Quit and window close request
- **Existing `tools.fxml`/`ToolsController`** reused untouched

## Files

### New

- `xpman-fx/src/main/java/com/ogerardin/xpman/settings/SettingsCategory.java`
- `xpman-fx/src/main/java/com/ogerardin/xpman/settings/SettingsPage.java`
- `xpman-fx/src/main/java/com/ogerardin/xpman/settings/GeneralController.java`
- `xpman-fx/src/main/java/com/ogerardin/xpman/settings/SettingsController.java`
- `xpman-fx/src/main/resources/fxml/settings/general.fxml`
- `xpman-fx/src/main/resources/fxml/settings/settings.fxml`

### Modified

- `xpman-fx/src/main/java/com/ogerardin/xpman/util/jfx/JfxAppPrefs.java` — add `boolean confirmQuit = true`
- `xpman-fx/src/main/java/com/ogerardin/xpman/util/jfx/JfxApp.java` — gate `quit()` on `getConfig().isConfirmQuit()`
- `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java` — `buildController` private→public; new `settings()` + `settings(SettingsCategory)` dialog opener; rewrite `manageTools()` → `settings(TOOLS)`
- `xpman-fx/src/main/resources/fxml/main.fxml` — add Settings... menu item with `Shortcut+Comma`
- `xpman-fx/src/main/java/com/ogerardin/xpman/shell/Section.java` — remove `TOOLS`
- `xpman-fx/src/main/java/module-info.java` — `opens com.ogerardin.xpman.settings to javafx.base, javafx.fxml`
- `xpman-fx/src/main/resources/css/xpman.css` — `.settings-*` classes
- `xpman-fx/src/test/java/com/ogerardin/xpman/util/test/JsonFileConfigPersisterTest.java` — `confirmQuit` default + round-trip tests
