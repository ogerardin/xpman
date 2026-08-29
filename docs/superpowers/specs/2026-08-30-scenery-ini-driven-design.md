# Ini-Driven Scenery Panel — Design

**Date:** 2026-08-30
**Status:** Design (approved)

## Goal

Make the scenery screen **ini-driven** (Approach A): the `scenery_packs.ini` is the
authoritative, ordered source of what is "listed". The table shows **one row per ini
entry, in file order** (Rank = 1-based position in the ini, counting every entry), then
**on-disk leftovers** (folders in `Custom Scenery` / `Global Scenery` not referenced by
any ini entry) appended at the bottom with no rank. Sorting by the user is removed —
the table always reflects the file order. The `*GLOBAL_AIRPORTS*` token resolves to the
**Global Airports** folder (version-aware) and becomes its actual row, matching
`GlobalAirportsSceneryPackage` (XP11 and XP12 both supported).

## Motivations

- Today the list is built from **disk scanning** (`Custom Scenery` + `Global Scenery` +
  non-standard `Custom Scenery (disabled)`), and the ini is only used to stamp a rank
  onto folders it happens to match (via `PathSceneryPackIniItem.indexOf`). Consequences:
  - A `SCENERY_PACK_DISABLED ...` line makes `SceneryPacks()`' `SceneryPack().star()`
    **stop parsing mid-file** — every entry after the first disabled line is silently
    dropped by the parser, and the `*GLOBAL_AIRPORTS*` token is never matched (see
    `SceneryManager.getSceneryPackage` comment). The ini is therefore a poor mirror of
    reality.
  - X-Plane's real loading order (the ini) can differ from what the panel shows, and
    "Fix scenery_packs.ini"/organize operates on a folder-based view.
- The ini (not the filesystem) is the single source of truth for X-Plane. Building the
  panel from it yields truthful order, explicit disabled entries, and a clear
  "not listed" bucket for unmanaged folders.

## Decisions

| Decision | Choice |
|---|---|
| Row model | NEW `SceneryEntry` (api, `com.ogerardin.xplane.scenery`) wraps `SceneryPackIniItem` (nullable), `SceneryPackage` (nullable), `Integer rank` (nullable). `SceneryManager` becomes `Manager<SceneryEntry>` |
| Enumeration | `SceneryEntryStatus`: `IN_INI`, `IN_INI_DISABLED`, `FOLDER_MISSING` (path entry, folder unresolvable), `TOKEN` (token entry, folder unresolvable), `NOT_LISTED` (disk leftover) |
| Display | Table rows = `SceneryEntry`s in list order; `Status` column replaces `Enabled?` (tri-state: Yes / No / blank); Rank column shows 1-based ini position, blank for leftovers |
| `enabled` | `SceneryPackage.enabled` stays **location-based** (`folder.startsWith(sceneryFolder)`, unchanged — drives the folder-move Enable/Disable toggle). Entry `isEnabled()` = `status == IN_INI \|\| status == NOT_LISTED` (used by context-menu guards and wizard filtering) |
| Ini path resolution | `SceneryPackIniItem.resolveFolder(Path baseFolder, Path globalAirportsFolder)` — polymorphic: path item → `baseFolder.resolve(folder)`; token → `globalAirportsFolder`. `globalAirportsFolder` = `xPlane.getPaths().globalAirports()` (version-aware: `Custom Scenery/Global Airports` on XP11, `Global Scenery/Global Airports` on XP12) |
| Disabled-folder compatibility | Non-standard `Custom Scenery (disabled)` is **not scanned** for leftovers. If a path entry's primary folder is missing but `<disabledSceneryFolder>/<name>` exists, resolve **there** (pack `enabled=false`) — this preserves the legacy folder-move Enable/Disable toggle without scanning the folder |
| Parse grammar | `SceneryPack() = SceneryPackDisabled() .or(SceneryPackEnabled()) .or(JunkLine())`. `SCENERY_PACK_DISABLED ` tried **first**; `JunkLine = noneOf("\r\n").star() seq(Newline) → null`; then filter nulls preserving order. Fixes mid-file truncation on disabled/unknown lines |
| Items | `SceneryPackIniItem` gains `disabled` flag (sealed base, `@EqualsAndHashCode(callSuper = true)` on subclasses); **single-arg constructors kept** (default `disabled=false`) so existing `SceneryIniFile11/12ParserTest` compile unchanged; add two-arg `of(String, boolean)`; `of(String)` delegates to `of(String, false)` |
| Constants | Move `GLOBAL_AIRPORTS_MARKER = "*GLOBAL_AIRPORTS*"` + new `GLOBAL_AIRPORTS_FOLDER = "Global Airports"` into `TokenSceneryPackIniItem`; `GlobalAirportsSceneryPackage` references them |
| Leftovers | Directories of `sceneryFolder` + `globalSceneryFolder` minus the real folders claimed by any ini entry → `NOT_LISTED` entries appended after all ini rows, no rank |
| Load | `loadPackages()` rebuilds entries: (1) parse ini → item list; (2) index scanned folders → `Map<Path, SceneryPackage>`; (3) per ini item resolve folder, `computeIfAbsent` package, `setEnabled(boolean) location-based`, `setRank(index+1)`, tag claimed set; (4) append leftovers; `items = entries`; fire LOADING/LOADED (unchanged pattern) |
| API surface | `getSceneryPackages()` (unchanged return type `List<SceneryPackage>`) = packages of all entries that have one, in list order. `enableSceneryPackage` / `disableSceneryPackage` / `moveSceneryPackageToTrash` / `install(Archive, ProgressListener)` kept; `install` unchanged |
| Cleanup | Remove dead `SCENERY_PACKAGE_COMPARATOR`; drop the 3rd/disabled-folder stream; drop the `sceneryFolder.getParent().relativize(...)+indexOf` rank logic |
| UI | `UiScenery` → **`UiSceneryEntry`** wrapping `SceneryEntry` (`@Delegate`, SpEL root = row instance); controller drops `SortedList`, keeps `ManagerItemsObservableList<SceneryEntry, UiSceneryEntry>`; `scenery.fxml` uses `status` + `sceneryClassName`, all columns `sortable="false"` except Rank (default asc, nullsLast) |
| Wizard rank | `Page2Controller` `Comparator.comparingInt(SceneryPackage::getRank)` NPEs on leftover entries (rank null and they are enabled) → null-safe comparator |
| Wizard input | `getSceneryPackages()` + filter `isEnabled()` (as today); leftovers are enabled → included; `SceneryOrganizer` re-writes the ini from that ordered list |
| Toolbar ▲/▼ | Stay non-functional (existing behavior, out of scope) |
| Out of scope | Writing/toggling `SCENERY_PACK_DISABLED` itself; organize dropping disabled/stale ini entries is pre-existing |

## Status derivation

```
iniItem == null                                   -> NOT_LISTED
package == null                                   -> FOLDER_MISSING (path item) | TOKEN (token item)
iniItem.isDisabled() || ! package.isEnabled()     -> IN_INI_DISABLED
otherwise                                         -> IN_INI
```

`IN_INI_DISABLED` therefore covers the two real ways a listed pack is off:
X-Plane-standard ini flag (`SCENERY_PACK_DISABLED ...`) and the legacy XPman
"moved to `Custom Scenery (disabled)`" state. Entry `isEnabled()` = `IN_INI` or `NOT_LISTED`.

## Behavioral changes (intended)

- `*GLOBAL_AIRPORTS*` (XP12, or path `Custom Scenery/Global Airports/` on XP11) now
  appears at its ini position with a rank (previously unranked, bottom).
- A `SCENERY_PACK_DISABLED ...` line no longer truncates the parse — later packs are
  listed, the disabled one shows Status=No.
- Disabling a pack keeps its row (Status=No) instead of losing rank; re-enabling works
  (folder move back).
- Folders under `Custom Scenery (disabled)` are no longer listed unless referenced by a
  still-resolvable ini entry.

## Files Changed

| Action | File |
|---|---|
| **Create** | `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryEntry.java` |
| **Create** | `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryEntryStatus.java` |
| **Modify** | `xpman-api/.../file/data/scenery/SceneryPackIniItem.java` (sealed base: `disabled`, `of(String, boolean)`, abstract `resolveFolder`) |
| **Modify** | `xpman-api/.../file/data/scenery/PathSceneryPackIniItem.java` (`@EqualsAndHashCode(callSuper=true)`, `resolveFolder`) |
| **Modify** | `xpman-api/.../file/data/scenery/TokenSceneryPackIniItem.java` (constants, `resolveFolder`) |
| **Modify** | `xpman-api/.../file/petitparser/SceneryPacksIniParser.java` (DISABLED-first-or-junk line grammar, filter nulls) |
| **Modify** | `xpman-api/.../scenery/GlobalAirportsSceneryPackage.java` (reference moved constants) |
| **Modify** | `xpman-api/.../scenery/SceneryManager.java` (`Manager<SceneryEntry>`, ini-driven `loadPackages`, resolution+fallback, remove comparator/rank logic) |
| **Rename** | `xpman-fx/.../panels/scenery/UiScenery.java` → `UiSceneryEntry.java` (wrap `SceneryEntry`; SpEL guards null-safe) |
| **Modify** | `xpman-fx/.../panels/scenery/SceneryController.java` (drop `SortedList`; `ManagerItemsObservableList<SceneryEntry, UiSceneryEntry>`) |
| **Modify** | `xpman-fx/src/main/resources/fxml/panels/scenery.fxml` (Status column + `SceneryStatusCellFactory`, `sceneryClassName`, `sortable="false"`) |
| **Create** | `xpman-fx/.../util/jfx/cell_factory/SceneryStatusCellFactory.java` (tri-state, mirrors `BooleanCellFactory`) |
| **Modify** | `xpman-fx/.../panels/scenery/wizard/Page2Controller.java` (null-safe rank sort) |
| **Modify** | `xpman-fx/.../scenery_organizer/SceneryOrganizer.java` or its caller (null-safe `sceneryClass(...)`: null package → `OtherSceneryClass.INSTANCE`) |
| **Create** | `xpman-api/src/test/resources/scenery_packs_disabled.ini` (disabled + comment + blank + unknown lines) |
| **Create** | `xpman-api/src/test/java/com/ogerardin/xplane/test/petitparser/SceneryIniFileDisabledParserTest.java` (ordered `contains(...)`, catches truncation) |
| **Modify** | `xpman-api/.../scenery/SceneryManagerTest.java` or new `@EnableOnLocalXPlane` test for token → `GlobalAirportsSceneryPackage` |

## Verification

- `mvn test -pl xpman-api` — parser suite green (existing 11/12 tests untouched, new
  disabled-mix test asserts full ordered list). Any real-X-Plane tests gated by existing
  `@EnableOnLocalXPlane*` annotations.
- `mvn -B -DskipTests clean package` — full reactor, as CI does.
- Manual sanity (local X-Plane install, if present): re-enabling a disabled pack via the
  context menu still moves the folder back; Global Airports row shows at its ini rank;
  leftovers sit below with blank Rank/Status.