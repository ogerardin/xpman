# XPman UI Modernization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Full visual + UX overhaul of the JavaFX UI: AtlantaFX dark-first theming, sidebar navigation, dashboard home, aircraft card grid with liveries, restyled wizards/dialogs.

**Architecture:** Theme-first phased approach on the existing FXML/controller/SpEL foundation; AtlantaFX user-agent theme + custom `xpman.css` override layer; ControlsFX kept for wizards/validation. Each phase ends with a working, better-looking app.

**Tech Stack:** JavaFX 25, AtlantaFX 2.1.0, Ikonli 12.4.0 (feather), ControlsFX 11.2.4, FXML, JPMS.

**Spec:** `docs/superpowers/specs/2026-08-24-ui-modernization-design.md`

**Verification commands** (after every task):
```bash
mvn -B -DskipTests clean package   # full build
mvn test                           # xpman-api + xpman-fx tests
```
Manual smoke run: `mvn -pl xpman-fx exec:java` or IDE run of `com.ogerardin.xpman.XPmanFX` with VM option `--add-opens=javafx.graphics/javafx.scene=org.controlsfx.controls`.

---

## Phase 1 — Theme foundation

### Task 1.1: Dependencies

**Files:**
- Modify: `xpman-fx/pom.xml`

- [ ] Add to `<dependencies>`:
```xml
<dependency>
    <groupId>io.github.mkpaz</groupId>
    <artifactId>atlantafx-base</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>org.kordamp.ikonli</groupId>
    <artifactId>ikonli-javafx</artifactId>
    <version>12.4.0</version>
</dependency>
<dependency>
    <groupId>org.kordamp.ikonli</groupId>
    <artifactId>ikonli-feather-pack</artifactId>
    <version>12.4.0</version>
</dependency>
```
- [ ] Build: `mvn -B -DskipTests clean package` — expect SUCCESS.
- [ ] Commit: `feat: add AtlantaFX and Ikonli dependencies`

### Task 1.2: JPMS module-info

**Files:**
- Modify: `xpman-fx/src/main/java/module-info.java`

- [ ] Add requires (feather `requires` is mandatory so its `IkonHandler` service is resolved on the module path):
```java
requires atlantafx.base;
requires org.kordamp.ikonli.core;
requires org.kordamp.ikonli.javafx;
requires org.kordamp.ikonli.feather;
```
- [ ] Build: `mvn -B -DskipTests clean package` — expect SUCCESS.
- [ ] Commit: `feat: declare AtlantaFX and Ikonli modules`

### Task 1.3: ThemeManager + prefs

**Files:**
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/util/jfx/ThemeManager.java`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/config/XPManPrefs.java`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java` (`setupStage`, ~line 212)

- [ ] `XPManPrefs` gains: `String theme = "dark";`
- [ ] `ThemeManager` API:
```java
public final class ThemeManager {
    public static void applySavedTheme()      // reads prefs, applies user agent stylesheet
    public static void toggle()               // dark<->light, persists, reapplies
    public static boolean isDark()
}
```
Implementation: `Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet())` / `PrimerLight`. Access prefs via the existing mechanism used by `XPmanFX` (see how `XPManPrefs` is loaded/saved; reuse it).
- [ ] Call `ThemeManager.applySavedTheme()` in `XPmanFX.setupStage` BEFORE creating the `Scene`.
- [ ] Build + test. Commit: `feat: theme manager with persisted dark/light preference`

### Task 1.4: Base stylesheet xpman.css

**Files:**
- Create: `xpman-fx/src/main/resources/css/xpman.css`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java` (add stylesheet to scene)

- [ ] Create `xpman.css` with app style classes using ONLY AtlantaFX functional color variables (so dark/light both work):
  - `.console` — dark inset background (`-color-bg-inset`), monospace, light text
  - `.warning-icon` — `-color-warning-emphasis` fill
  - `.validation-cell` / `.validation-cell-invalid` — monospace; invalid uses `-color-danger-fg`
  - `.segment-*` — per-category segment colors (migrate `CategorySegmentView`/`SizeInfoNode` inline colors)
  - `.empty-state` — centered icon + message styles
  - `.hyperlink-black` — absorb `css/hyperlink.css` (then DELETE `css/hyperlink.css` and its reference in `util/jfx/menu/MethodHyperlink.java:22`)
- [ ] Add `scene.getStylesheets().add(...xpman.css...)` in `setupStage`.
- [ ] Build + test + manual: app renders in PrimerDark. Commit: `feat: add xpman.css override layer`

### Task 1.5: Replace inline styles

**Files:**
- Modify: `xpman-fx/src/main/resources/fxml/console.fxml:26-28` → `styleClass="console"`, drop hardcoded InputMono font
- Modify: `xpman-fx/src/main/resources/fxml/panels/scenery.fxml:80,85` → CSS class for CENTER-RIGHT alignment
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/xplane/XPlaneController.java:105` → Ikonli FontIcon `fe-alert-triangle` + `.warning-icon`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/util/jfx/cell_factory/ValidatingEditingCell.java:22,49` → style classes
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/xplane/breakdown/CategorySegmentView.java:31,35`, `SizeInfoNode.java:23` → style classes
- [ ] Build + test + manual. Commit: `refactor: replace inline styles with xpman.css classes`

### Task 1.6: Icons swap

**Files:**
- Modify: every file importing ControlsFX `org.controlsfx.glyphfont.FontAwesome` (grep to enumerate; known: `XPlaneController.java:19-20`)

- [ ] Replace with `org.kordamp.ikonli.javafx.FontIcon` using feather literals (`fe-*`); set `iconSize`/`iconColor` or CSS classes.
- [ ] Build + test + manual. Commit: `refactor: replace ControlsFX FontAwesome glyphs with Ikonli feather icons`

### Task 1.7: Theme toggle menu item

**Files:**
- Modify: `xpman-fx/src/main/resources/fxml/main.fxml` (add View ▸ Toggle theme item)
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java` (handler)

- [ ] Menu item text reflects current theme ("Switch to Light Theme"/"Switch to Dark Theme"); handler calls `ThemeManager.toggle()`.
- [ ] Build + test + manual: toggle works live, persists across restart. Commit: `feat: theme toggle menu item`

---

## Phase 2 — Shell & navigation

### Task 2.1: Section enum + Sidebar control

**Files:**
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/shell/Section.java`
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/shell/SidebarController.java`
- Create: `xpman-fx/src/main/resources/fxml/shell/sidebar.fxml`
- Modify: `module-info.java` (exports/opens for new package)

- [ ] `Section` enum: `HOME("Home", "fe-home"), AIRCRAFT("Aircraft", "fe-plane"), SCENERY("Scenery", "fe-map"), NAVDATA("Nav data", "fe-navigation"), PLUGINS("Plugins", "fe-plug"), TOOLS("Tools", "fe-tool")` — label + iconLiteral + `contentFxml` path + accelerator.
- [ ] `sidebar.fxml`: VBox of section buttons (FontIcon + Label) + footer (theme toggle, About, version). Selection styled with accent pill (`.sidebar-item`/`.sidebar-item:selected` in `xpman.css`).
- [ ] `SidebarController`: `ObjectProperty<Section> selectedSection`; `select(Section)`; theme toggle button (sun/moon icon swap); About hyperlink reusing existing handler target.
- [ ] Build + test. Commit: `feat: sidebar navigation control`

### Task 2.2: main.fxml rewrite

**Files:**
- Modify: `xpman-fx/src/main/resources/fxml/main.fxml`

- [ ] Replace `TabPane` with `BorderPane`: `left` = `fx:include shell/sidebar.fxml`, `center` = `StackPane fx:id="contentArea"`. Keep MenuBar. Keep overall structure otherwise.
- [ ] Build + test. Commit: `refactor: replace TabPane with sidebar shell in main.fxml`

### Task 2.3: Lazy section loading

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java`

- [ ] `Map<Section, Node> sectionCache`; `showSection(Section)` loads the panel FXML on first access (reuse existing `buildController` controllerFactory mechanism), caches, swaps into `contentArea`. HOME→`panels/xplane.fxml` (until Phase 3), AIRCRAFT→`panels/aircraft.fxml`, SCENERY→`panels/scenery.fxml`, NAVDATA→`panels/navdata.fxml`, PLUGINS→`panels/plugins.fxml`.
- [ ] Listen to sidebar `selectedSection`; default HOME.
- [ ] Manual: switching sections preserves panel state (managers not reloaded — LOADING/LOADED events already handled by panels).
- [ ] Build + test. Commit: `feat: lazy section loading wired to sidebar`

### Task 2.4: Tools as section

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java` (remove Tools stage code ~lines 62-71; menu item selects section instead)

- [ ] TOOLS section loads `tools/tools.fxml` into contentArea; delete separate-stage launch; Tools menu item calls `showSection(TOOLS)`.
- [ ] Build + test + manual: Tools shows inline, no second window. Commit: `feat: fold Tools manager into main window`

### Task 2.5: Window sizing

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java` (`setupStage`)

- [ ] Default 1100x700, `stage.setMinWidth(900)`, `setMinHeight(600)`; keep position persistence logic untouched.
- [ ] Build + test. Commit: `feat: larger default window with minimum size`

### Task 2.6: Keyboard shortcuts

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java` or sidebar (scene accelerators)

- [ ] Alt+1…6 and Shortcut+1…6 select sections (scene `Mnemonic`s or menu items with accelerators; simplest: add invisible menu items or `scene.addMnemonic`).
- [ ] Build + test + manual. Commit: `feat: keyboard shortcuts for section navigation`

### Task 2.7: Sidebar footer polish

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/shell/SidebarController.java`, `xpman.css`

- [ ] Footer: theme toggle icon button (sun/moon per current theme), About button (opens existing About stage), version label (source: same version string the title/About uses).
- [ ] Build + test + manual. Commit: `feat: sidebar footer with theme toggle, about, version`

---

## Phase 3 — Panels

### Task 3.1: Dashboard home

**Files:**
- Create: `xpman-fx/src/main/resources/fxml/panels/home.fxml`
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/home/HomeController.java`
- Modify: `Section.HOME` contentFxml → `panels/home.fxml`; module-info opens
- Reuse: existing `panels/xplane.fxml` content embedded or absorbed; `breakdown.fxml` included as-is (rethemed by CSS)

- [ ] Layout: hero row (X-Plane logo, version/folder info as styled labels + hyperlink, "Start X-Plane" primary button) + prominent "Install anything…" secondary button; status row: SegmentedBar disk-usage include + update notification banners (styled `.banner-warning`/`.banner-info` in `xpman.css` replacing TextFlow look); count tiles (aircraft/scenery/plugins/navdata counts, clickable → `showSection`).
- [ ] Data: reuse `XPlaneController`'s sources (XPlaneProperty, usage breakdown, notifications) — move logic into `HomeController` or keep `XPlaneController` as child include; counts from managers' item lists (already observable).
- [ ] Build + test + manual. Commit: `feat: dashboard home panel`

### Task 3.2: Install anything

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/home/HomeController.java`
- Possibly modify: `xpman-fx/src/main/java/com/ogerardin/xpman/install/wizard/InstallWizard.java` + `Page1Controller` (verify null-InstallType path)

- [ ] "Install anything…" opens `InstallWizard` with NO preselected install type (universal installer path from File menu — `XPmanFX.java:236` — already does this; reuse that invocation).
- [ ] Build + test + manual with a sample archive. Commit: `feat: universal install entry on home dashboard`

### Task 3.3: Aircraft card grid

**Files:**
- Create: `xpman-fx/src/main/resources/fxml/panels/aircraft/aircraft-card.fxml` + `AircraftCardController`
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/aircraft/AircraftCard.java` (custom control)
- Modify: `xpman-fx/src/main/resources/fxml/panels/aircraft.fxml` (table → ScrollPane+FlowPane grid, keep toolbar + filter + new search field)
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/aircraft/AircraftsController.java`

- [ ] Card: thumbnail (reuse `PathImageCell` image-loading path), name, author/studio, spec-version badge; hover shows quick-action icon buttons built from existing `UiAircraft` annotated methods (reuse `IntrospectionHelper`/`MethodAction` machinery via `ActionsCellFactory`-style logic adapted for cards); context menu on card = same reflection-driven menu the table had.
- [ ] Grid: `ScrollPane` + `FlowPane` populated from the existing `ManagerItemsObservableList` (listen and rebuild/update cards). Filter combo kept; add text search `TextField` filtering by name/author.
- [ ] Fallback: if huge libraries perform badly, note it; virtualized grid is out of scope unless blocking.
- [ ] Build + test + manual with real X-Plane install. Commit: `feat: aircraft card grid with search and filter`

### Task 3.4: Expandable liveries

**Files:**
- Modify: aircraft card classes from Task 3.3
- Delete: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/aircraft/TreeAircraftsController.java`, `xpman-fx/src/main/resources/fxml/panels/aircraftsTree.fxml`

- [ ] Card shows "N liveries" badge when `aircraft.getLiveries()` non-empty; expanding (chevron button) reveals a row of livery thumbnails (smaller cards) with `UiLivery` actions (reveal, move to trash — see `TreeAircraftsController.java:100-111` and `UiLivery`).
- [ ] Delete the parked tree files after logic is salvaged.
- [ ] Build + test + manual. Commit: `feat: expandable liveries on aircraft cards`

### Task 3.5: Tables modernized

**Files:**
- Modify: `xpman.css` (table polish: header, row hover, spacing, badge styles)
- Modify: `fxml/panels/scenery.fxml`, `navdata.fxml`, `plugins.fxml` (toolbar buttons get feather icons + labels)

- [ ] Scenery ▲▼ buttons KEPT AS-IS (no handler — implementation deferred by decision).
- [ ] Build + test + manual. Commit: `feat: modernized scenery/navdata/plugins tables`

### Task 3.6: Empty & loading states

**Files:**
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/util/jfx/EmptyState.java` (or FXML component)
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/Controller.java` (base class placeholder logic)
- Modify: panel FXMLs/controllers to set empty states

- [ ] `EmptyState(iconLiteral, message, optionalButton)` node; loading state = themed spinner (ProgressIndicator styled) replacing `loading.gif`; empty state message + "Install anything…" button where relevant.
- [ ] Build + test + manual (empty state visible when no X-Plane configured). Commit: `feat: empty and loading states for panels`

---

## Phase 4 — Wizards & dialogs

### Task 4.1: Wizard restyle

**Files:**
- Modify: `xpman.css` (ControlsFX wizard selectors: `.wizard`, buttons, borders; validation decoration override classes)
- Possibly modify: `util/jfx/wizard/ValidatingWizardPane.java` (step indicator header)

- [ ] Wizard chrome restyled: header band with icon + page title, step indicator (1·2·3: Source → Review → Progress), flat themed buttons, validation decoration recolored via `-color-danger-*`/`-color-success-*`.
- [ ] Build + test + manual: run install wizard with sample archive. Commit: `feat: restyled install/organize wizards`

### Task 4.2: Progress page

**Files:**
- Modify: `xpman-fx/src/main/resources/fxml/install_wizard/page3.fxml` (+ its controller)

- [ ] Progress bar + scrolling log area styled `.console`.
- [ ] Build + test + manual. Commit: `feat: wizard progress page with live log`

### Task 4.3: Dialogs restyle

**Files:**
- Modify: `fxml/inspectionResults.fxml`, `diag/SeverityIconCellFactory.java` (Ikonli severity icons + color badges), `fxml/about.fxml` (drop hardcoded fonts), `util/jfx/ErrorDialog.java` (themed alert + Ikonli)

- [ ] Build + test + manual: run an inspection, trigger an error dialog. Commit: `feat: restyled inspection/about/error dialogs`

### Task 4.4: WebView theming

**Files:**
- Modify: `util/jfx/WebViewStage.java`

- [ ] Inject user-agent CSS matching active theme (dark bg/light text in dark mode) — small inline `<style>` or `webView.getEngine().setUserStyleSheetLocation` with a data URL.
- [ ] Build + test + manual: open a navdata description popup. Commit: `feat: theme-matched WebView popups`

---

## Phase 5 — UX polish & cleanup

### Task 5.1: Toasts

**Files:**
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/util/jfx/Toast.java`
- Modify: install success paths + action success handlers

- [ ] Bottom-right transient toast (auto-dismiss ~4s), `.toast` in `xpman.css`; replaces bare success Alerts. Use for install completion and `@OnSuccess` action feedback where alerts are currently shown.
- [ ] Build + test + manual. Commit: `feat: toast notifications for success feedback`

### Task 5.2: Themed confirms

**Files:**
- Modify: `util/jfx/menu/MethodAction.java` (or wherever `@Confirm` confirmation Alert is built)

- [ ] Confirmation dialogs themed: danger accent styling for destructive actions (`-color-danger-emphasis`).
- [ ] Build + test + manual: e.g. move-to-trash confirm. Commit: `feat: themed confirmation dialogs`

### Task 5.3: Cleanup + README

**Files:**
- Modify: remove commented-out FXML blocks (`aircraft.fxml`, etc. — verify each)
- Modify: `README.md` + new screenshots in `assets/screenshots/`

- [ ] Remove dead commented FXML; refresh README screenshots to the new UI (manual screenshots by user or agent if display capture available).
- [ ] Final full verify: `mvn -B -DskipTests clean package`, `mvn test`, full manual pass. Commit: `chore: cleanup and updated screenshots`

---

## Out of scope (explicit)

- Scenery ▲▼ reorder implementation (buttons kept, non-functional, deferred)
- xpman-api changes of any kind
- Virtualized card grid (unless blocking)
- Custom window chrome
