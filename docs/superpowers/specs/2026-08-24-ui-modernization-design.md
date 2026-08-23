# XPman UI Modernization — Design

**Date:** 2026-08-24
**Status:** Approved
**Branch:** `feat/ui-modernization`

## Goal

Full visual and UX overhaul of the XPman JavaFX desktop UI: modern flat dark-first
theming, sidebar navigation, a dashboard landing page, an aircraft card grid with
expandable liveries, and restyled wizards/dialogs — while preserving the existing
FXML/controller conventions, the SpEL/reflection action framework, JPMS structure,
and JavaFX 25.

## Decisions

| Decision | Choice |
|---|---|
| Scope | Full overhaul, phased (theme-first) |
| Aesthetic | Modern flat, dark-first (Primer-style, VS Code/Linear vibe) |
| Theme | AtlantaFX 2.1.0 PrimerDark/PrimerLight + custom `xpman.css` override layer using functional color variables only |
| Light/dark | Dark default + runtime toggle persisted in `~/.xpman` (`XPManPrefs.theme`) |
| Icons | Ikonli 12.4.0 + feather pack (replaces ControlsFX FontAwesome glyphs); existing static PNGs (app icon, dialogs) stay |
| Navigation | Labeled left sidebar; Tools folded in as section 6; native window chrome kept |
| Home | Dashboard: X-Plane status card, "Install anything...", disk-usage breakdown, update banners, clickable counts |
| Aircraft | Card grid with expandable liveries (completes the parked `TreeAircraftsController`/`UiLivery` concept) |
| Other panels | Modernized tables; proper empty/loading states |
| Scenery ▲▼ | Kept as-is; reordering implementation explicitly deferred (this work is UI-only) |
| Wizards | ControlsFX engine kept, restyled (header band, step indicator, themed validation) |
| Dialogs | Console/inspection/about/error restyled; WebView popups theme-matched |
| UX | Unified install entry point, toasts for success, themed confirms, aircraft search field |
| Window | Native chrome; default ~1100x700, min ~900x600; position persistence kept |

## Design System

- **Base**: AtlantaFX `atlantafx-base` 2.1.0; `PrimerDark` default, `PrimerLight`
  alternate; applied via `Application.setUserAgentStylesheet()` before scene creation.
- **Custom layer**: single `css/xpman.css` scene stylesheet. Uses only AtlantaFX
  functional color variables (`-color-bg-default`, `-color-bg-subtle`,
  `-color-fg-default`, `-color-fg-muted`, `-color-border-muted`, `-color-accent-*`,
  `-color-danger-*`, `-color-success-*`, …) so dark and light both stay correct.
  Defines app style classes: `.sidebar`, `.sidebar-item`, `.card`, `.console`,
  `.warning-icon`, `.validation-cell`, `.empty-state`, segment colors, etc.
- **Typography**: system font stack via the theme; section titles 16–18px semibold,
  body 13px, muted secondary text. No hardcoded font families in FXML.
- **Icons**: Ikonli `FontIcon` with feather pack (`fe-*` literals), usable directly
  in FXML. JPMS requires `requires org.kordamp.ikonli.feather` so the icon handler
  service is resolved on the module path.

## App Shell

- `BorderPane`: labeled left sidebar (~220px) + `StackPane` content area. MenuBar
  stays (native macOS menu via nsmenufx preserved).
- Sections: **Home**, **Aircraft**, **Scenery**, **Nav data**, **Plugins**, **Tools**.
- Panels lazy-loaded on first selection, cached; existing panel controllers and
  manager bindings untouched.
- Sidebar footer: theme toggle (sun/moon), About, version label.
- Keyboard: Alt+1…6 (and Shortcut+1…6) select sections.

## Panels

- **Home**: X-Plane status card (logo, version, folder hyperlink, Start X-Plane),
  "Install anything…" prominent button (universal installer, no preselected type),
  disk-usage SegmentedBar (rethemed `breakdown.fxml`), update notification banners,
  clickable count tiles that jump to sections.
- **Aircraft**: card grid — thumbnail, name, author/studio, spec-version badge,
  hover quick actions (from existing annotated `UiAircraft` methods), context menu
  preserved. Filter combo + text search field. Cards with liveries show an
  "N liveries" badge; expanding shows livery thumbnails and `UiLivery` actions
  (reveal, move to trash). `TreeAircraftsController`/`aircraftsTree.fxml` removed
  after its livery logic is salvaged into cards.
- **Scenery / Nav data / Plugins**: rethemed tables/tree-tables (headers, row hover,
  spacing), toolbar buttons with icons + labels. Scenery ▲▼ buttons kept as-is.
- **Empty & loading states**: reusable empty-state component (icon + message +
  optional action) and themed loading state replacing `loading.gif` placeholder.
- **Tools**: current SplitPane layout embedded as section 6; separate stage removed.

## Wizards & Dialogs

- ControlsFX `Wizard` engine kept; CSS restyle: modern header band, step indicator
  (Source → Review → Progress), flat buttons, recolored validation decoration.
- Progress page: progress bar + live log lines with console styling.
- Console dialog: terminal look via `.console` style class (works in both themes).
- Inspection results: Ikonli severity icons + color badges.
- About/Error dialogs: theme typography, Ikonli severity icons, danger styling.
- `WebViewStage`: inject user-agent CSS matching active theme (no white flash).

## UX Flows

- Unified install: "Install anything…" (home + File menu); per-panel Install…
  buttons remain as scoped shortcuts into the same wizard.
- Success feedback via transient toasts (bottom-right, auto-dismiss).
- `@Confirm` dialogs themed with danger accent for destructive actions.
- Context menus restyled with Ikonli icons; common actions also on card hover.
- Cleanup: commented-out FXML blocks removed, `hyperlink.css` folded into
  `xpman.css`, README screenshots updated.

## Technical Notes

- New deps in `xpman-fx/pom.xml`: `io.github.mkpaz:atlantafx-base:2.1.0`,
  `org.kordamp.ikonli:ikonli-javafx:12.4.0`, `org.kordamp.ikonli:ikonli-feather-pack:12.4.0`.
- `module-info.java`: `requires atlantafx.base; requires org.kordamp.ikonli.core;
  requires org.kordamp.ikonli.javafx; requires org.kordamp.ikonli.feather;`
- `XPManPrefs` gains `String theme` (default `"dark"`).
- No xpman-api changes (scenery reorder deferred).
- IDE dev runs still need `--add-opens=javafx.graphics/javafx.scene=org.controlsfx.controls`.

## Phases

1. **Theme foundation** — deps, JPMS, ThemeManager, `xpman.css`, inline-style
   removal, Ikonli swap, theme toggle menu item.
2. **Shell** — sidebar, main.fxml rewrite, lazy section loading, Tools fold-in,
   window sizing, shortcuts, sidebar footer.
3. **Panels** — dashboard home, install anything, aircraft card grid + liveries,
   table modernization, empty/loading states.
4. **Wizards & dialogs** — wizard restyle, progress page, console/inspection/
   about/error, WebView theming.
5. **UX polish** — toasts, themed confirms, cleanup, README screenshots.

Each phase ends with a working app: `mvn -B -DskipTests clean package`,
`mvn test`, and a manual smoke run.
