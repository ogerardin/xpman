# Nav Data Screen — Redesigned Layer Stack

**Date:** 2026-09-04
**Status:** Design (approved)

## Goal

Replace the nav data **tree table** (data sets → files) with a **layered card stack**:
one card per `NavDataSet`, ordered by priority, showing a status badge, expandable
file rows (icon, size, last-modified, AIRAC cycle), a per-item `inspect()`/check action
backed by the inspection framework, and a native help dialog. Icons are Feather (`fth-*`),
one per file type; the PNG info icon is dropped.

## Motivations

- The current `navdata.fxml` is a 5-column `TreeTableView` (Name / Exists / AIRAC Cycle /
  Metadata / Build) with HTML help popups (`WebViewStage`) and a PNG info icon — visually
  inconsistent with the card-based panels (aircraft, tools) and the app's Ikonli Feather
  standard.
- X-Plane treats nav data as a **priority stack** of layers; a card stack makes that model
  visible ("Layer N/6").
- `NavDataSet.inspect()` is a TODO ("check that all files exist"). The inspection
  framework exists and other domain objects (`UiPlugin`, `UiAircraft`, `UiSceneryEntry`)
  already expose `inspect()` with `@OnSuccess("displayInspectionResults(#result)")`.

## Decisions

| Decision | Choice |
|---|---|
| Layout | `navdata.fxml` → `BorderPane`: toolbar (Reload) top, center `StackPane(ScrollPane fitToWidth > VBox#cardsPane, placeholderPane)` — mirrors `aircraft.fxml`. Tree table removed |
| Card | NEW `NavDataSetCardView extends VBox` (programmatic, like `AircraftCardView`): header (layer icon, name, "Layer N/6" badge, status badge, chevron), description line, expandable file rows |
| Order | `NavDataManager.loadNavDataSets()` order (sim-wide override → base → updated base → FAA → hand-placed → user data), increasing priority, top-to-bottom |
| File row | per-file icon, name (`NavDataItem.getName()`, path relative to base folder), right-aligned meta: size (`Files.size`), last-modified, AIRAC cycle (`getAiracCycle()`) |
| Placeholder | `EmptyState` (`fth-navigation`) / `EmptyState.loading(...)`, like `AircraftsController.updateCards()` |
| Status badge | Computed in `NavDataController` from `getExists()` + `getAiracCycle()` per file: OK (all exist, one cycle) / MISSING (some files absent) / CYCLE_MISMATCH (>1 distinct non-null cycle) |
| Controller base | `NavDataController extends Controller` (currently extends nothing) — required for `@OnSuccess("displayInspectionResults(#result)")` (uses inherited `displayInspectionResults`) |
| Items list | `ManagerItemsObservableList<NavDataSet, NavDataSet>` with `Function.identity()` → **`ManagerItemsObservableList<NavDataSet, UiNavDataItem>`** with `UiNavDataItem::new`; cards rebuilt on list change like `AircraftsController.updateCards()` (tree-building code in `initialize()`/`treeItem()` removed) |
| Inspect action | Implement `NavDataSet.inspect()` (see below). NEW `UiNavDataItem.inspect()` → `item instanceof Inspectable i ? i.inspect() : InspectionResult.empty()`, annotated `@OnSuccess("displayInspectionResults(#result)")`, `@Confirm`-free; reuses existing results dialog |
| Card actions | Hover buttons + context menu via `GenericContextMenuFactory<UiNavDataItem>` / `MethodButton` (existing framework): `reveal` (annotated, existing), `inspect` (new). `help` is **card-level** (info button in header + hover action), NOT a `UiNavDataItem` method — the annotation framework can't supply a window owner for the dialog |
| Help dialog | NEW `NavDataInfoDialog` (navdata package): native JavaFX `Dialog` (replaces the `WebViewStage` popup entirely): item name, description, folder path, cycle/metadata/build, hyperlink to the X-Plane navdata article; styled with xpman.css classes. Shown from `NavDataSetCardView` with `initOwner(card.getScene().getWindow())` |
| Icons | Replace PNG `img/dialog-information.png` usage with Ikonli `fth-*` (only installed pack). Per-file map below |
| CSS | New classes in `xpman.css`, **only `-color-*` variables**: `.navdata-card`, `-header`, `-name`, `-layer-badge`, `-status-badge` (+ `-ok`/`-warning`/`-error`), `-file-row`, `-file-icon`, `-file-name`, `-file-meta`, `-chevron` |

### `NavDataSet.inspect()` behavior

Per `NavDataItem` in the data set (files + extra children):

- file does not exist → `InspectionMessage` **ERROR** `"File not found: <name>"`
- collect distinct non-null cycles among existing files:
  - count > 1 → **WARNING** `"Mixed AIRAC cycles: <cycles>"` (article rule: all files must be the same cycle)
  - count == 1 → INFO `"OK — cycle <cycle>"`
  - count == 0 → INFO `"No data present"` (all files missing)
- appended via `InspectionResult.append` / plain list → `InspectionResult.of(...)`

### File-type icon map (by filename)

| File | Icon |
|---|---|
| `earth_nav.dat` | `fth-navigation` |
| `earth_fix.dat` | `fth-map-pin` |
| `earth_awy.dat` | `fth-route` |
| `earth_hold.dat` | `fth-anchor` |
| `earth_mora.dat`, `earth_msa.dat` | `fth-layers` |
| `earth_424.dat` | `fth-globe` |
| `airspace.txt` | `fth-airplay` |
| `atc.dat` | `fth-radio` |
| `CIFP` (folder) | `fth-folder` |
| `user_nav.dat`, `user_fix.dat` | `fth-user` |
| default | `fth-file-text` |

### Layer icon map (by `NavDataSet`)

| Layer | Icon |
|---|---|
| Sim-wide ARINC424 override | `fth-globe` |
| Base | `fth-database` |
| Updated base | `fth-refresh-cw` |
| FAA updated approaches | `fth-flag` |
| Hand-placed localizers | `fth-map-pin` |
| User data | `fth-user` |

## Out of scope

- Search/filter box (considered, not selected)
- Writing/toggling nav data files, moving layers, modifying `scenery`-style ini
- Any change to `NavDataFile` parsing or the `DatFile` subsystem

## Tests

- NEW `xpman-api` `NavDataSetTest` (JUnit 5, real X-Plane gated like other api tests):
  - `inspect()` returns ERROR message for a missing file
  - `inspect()` returns WARNING for mixed cycles (sample dat files with differing headers in test resources)
  - `inspect()` returns OK for a consistent set / empty set
- No new xpman-fx tests (fx suite is minimal).

## References

- https://developer.x-plane.com/article/navdata-in-x-plane-11/