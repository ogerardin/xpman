# FAA CIFP (ARINC 424) Installable Type

**Date:** 2026-09-30 (rewritten 2026-10-01 after implementation verification)
**Status:** Implemented

## Goal

Let XPman install free FAA CIFP (ARINC 424) navigation data. The user drops the FAA cycle
ZIP on the existing install wizard; XPman extracts `FAACIFP18` and writes it into
`Custom Data` as `earth_424.dat`, which X-Plane reads as a sim-wide ARINC 424 override.

The navdata panel then states plainly that this layer makes X-Plane ignore every other
navdata layer, and warns that the FAA dataset covers the US only.

## Motivations

- FAA CIFP is free, current-cycle (28-day), and needs no Navigraph subscription. The
  manual procedure is small but non-obvious: download the current cycle, find the
  extensionless `FAACIFP18` inside it, and rename it to `earth_424.dat` in the right
  `Custom Data` folder.
- XPman already models `earth_424.dat` as the *Sim-wide ARINC424 override* navdata layer
  (`NavDataManager.simWideOverride()`) and reports on it, but offered no way to install one.
- The wizard is fully generic: `Page2Controller` builds a `GenericInstaller` over an
  `ArchiveInstallSource`, which resolves the concrete type by ClassGraph discovery. A new
  `InstallableType` is picked up with **zero UI changes**. `NavDataController.install()`
  already launches that wizard.

## Decisions

| Decision | Choice |
|---|---|
| Input | User-selected FAA cycle ZIP from `https://www.faa.gov/air_traffic/flight_info/aeronav/digital_products/cifp/download/`. XPman never discovers or downloads the cycle |
| Recognition | Archive contains an entry whose **file name is exactly `FAACIFP18`** (FAA ships it extensionless). No overlap with `NavDataInstallableType`, which matches `earth_*.dat` |
| Install target | `Custom Data/earth_424.dat` — the name X-Plane actually reads |
| Conversion | **None.** See "No converter" below |
| Coverage warning | `Severity.WARN` in `preconditions()`, so wizard page 2 shows it before the user commits; only `ERROR` blocks the Next button |
| Coverage detection | ARINC 424 `HDR01` record, cols 6–15 carry the dataset's own name. FAA datasets are named `FAACIFP*` |
| New UI | Four CSS rules. No FXML or controller wiring changes |
| Manager | **None.** The install is one extract and one copy |

## No converter

The original design of this feature ran Philipp Münzel's `convert424toxplane` over the
source to generate `earth_nav/fix/awy.dat` and a `CIFP/` folder for `Custom Data`. Manual
verification showed this is wrong, on the authority of X-Plane's own documentation:

- <https://developer.x-plane.com/article/navdata-in-x-plane-11/>: *"After this file has been
  read, X-Plane will not load any other information from other text files… In particular,
  X-Plane will then NOT load any of the files described in the following as 'Global data'."*
  `earth_nav/fix/awy.dat` and `CIFP/` are exactly those other files, so the converter's
  output is ignored whenever `earth_424.dat` is present.
- The same page names `convert424toxplane` a tool for **data providers** converting a master
  `.dat` into X-Plane-native XPNAV1200 for distribution. Its signature is
  `convert424toxplane <masterfile> "<copyright>" <path to apt.dat>` — the third argument is an
  airport database we do not have, and the forum procedures that "work" pass a meaningless
  placeholder for the copyright string.
- Its output covers the US only, so copying it into `Custom Data` would *replace* the global
  base layer and delete navdata for the rest of the world.

Deleting the converter removed: a 233-line `CifpManager`, two `Platform` hooks spread over
four platform classes plus the `XPlane` wiring, a 26 MB third-party download, a subprocess
call, a cached `geoids/` dependency that the archive ships only under `windows/` but which
macOS and Linux equally require, and an executable-permission problem on extracted binaries.

Net cost of the whole feature against `main`: **409 production lines** (391 net) plus 319
test lines. Of the production lines, the installer itself is 84; the rest is the navdata
panel and coverage reporting.

## Components

### `CifpInstallableType` (`com.ogerardin.xplane.install.types`)

Public no-arg constructor (required by `IntrospectionHelper.findAllSubclasses`). Mirrors
`NavDataInstallableType`'s shape: matches on `Archive`, does its own filesystem work.

`install()` locates the `FAACIFP18` entry, extracts **only** that entry into a temp
directory via the existing `Archive.extract(folder, Predicate, progress)` overload, copies it
to `Custom Data/earth_424.dat`, and reloads the navdata manager.

Two deliberate choices:

- **Extract to a temp directory** rather than straight into `Custom Data`. The real FAA ZIP
  has `FAACIFP18` at the root, but a nested entry would otherwise leave a stray
  `Custom Data/CIFP/<date>/` tree. `FileUtils.deleteQuietly` in a `finally` is the existing
  idiom.
- **`Files.copy`, not `Files.move`.** A temp directory and the X-Plane install can sit on
  different volumes on Windows, where `move` throws `AtomicMoveNotSupportedException`.

### `Arinc424Header` (`com.ogerardin.xplane.file.data.dat`)

A record of the dataset's self-declared name and its originator, read from the first five
lines in fixed-width columns:

| Field | Columns | Carried by |
|---|---|---|
| Record identifier | 1–5 | all records (`HDR01`…`HDR05`) |
| Dataset name | 6–15 | `HDR01` only |
| Free text | 39– | `HDR02`…`HDR05` |

`HDR01` is *not* used for the originator: from column 36 it carries a structured preamble
(`2610  09-SEP-2026…  U.S.A. DOT FAA  …  113023A4`), so taking its col-39 substring yields
the preamble rather than a name. `HDR02` holds the originator's plain name.

These files run to **50 MB and more**, so the parser reads five lines with a `BufferedReader`
and nothing else. Measured against a real FAA cycle: 55 ms to read the header, 3 ms for a
full `inspect()`. `DatFile` / `DatFileParser` could not be reused here — `XPlaneFile` reads
the whole URI into a `String` before parsing, and `DatFileParser` only understands the
X-Plane-native XPNAV header (`I` / `1100 version` / `data cycle …`), so on an ARINC 424 file
it yields null and never a cycle.

### `Arinc424NavDataFile` (`com.ogerardin.xplane.navdata`)

A `NavDataFile` that answers `getAiracCycle()`, `getMetadata()`, `getBuild()` and
`getDatasetName()` from `Arinc424Header` instead of from `DatFile`. Overriding all of them is
what keeps the 50 MB file from being slurped into a `String`: the inherited implementations
route through `getData()`, which parses the entire file to reach a header.

Selected polymorphically by `NavDataSet.createFile(Path)`, which `Arinc424DataSet` overrides.

### `NavDataSet` / `Arinc424DataSet`

Two hooks, both overridable rather than branched on:

- `isOverriding()` — false by default. True only for the sim-wide override. It is a
  **per-instance flag**, not a per-type one, because `simWideOverride()` and
  `faaUpdatedApproaches()` are both `Arinc424DataSet`.
- `coverageMessage()` — `Optional<InspectionMessage>`, empty by default, overridden by
  `Arinc424DataSet` to warn when the present file is an FAA dataset.

`inspect()` gained a third summary case. It previously reported `No data present` whenever
no cycle could be parsed, which is wrong for ARINC 424: the file is present and perfectly
valid, it simply has no XPNAV cycle marker. Now: files present but unparseable →
`Present (AIRAC cycle unknown)`; no files → `No data present`.

### Panel changes (`xpman-fx`)

- `NavDataController.updateCards()` finds the highest present overriding layer and passes its
  1-based index to each card. Cards below it get `ignored`.
- `NavDataSetCardView` adds `.navdata-card-ignored` (50% opacity) plus an `ignored by layer N`
  badge; the overriding card gets an `overrides N layers` badge.
- `buildStatusLabel()` now picks the **most severe** inspection message rather than the last
  one. It previously assumed `inspect()` ends with the summary message, so appending a
  coverage warning would have displaced the cycle display. Ranking keeps healthy layers on
  their cycle and surfaces warnings where the eye already looks.

## Coverage detection, and its limits

Only the FAA dataset is recognized as partial. Its header names itself `FAACIFP18`; a
commercial 424 master says nothing about its coverage area, so we make no claim rather than
guess.

General partial-coverage detection is possible but was scoped out: region identifiers live in
per-record-type fixed-width fields and are frequently `UNKUNK` even in FAA data, so it needs
per-record-type parsing plus a maintained list of region codes, over a 50 MB file.

ARINC 424 carries no XPNAV cycle marker, so the override's AIRAC cycle stays unknown. The
FAA header does carry an unambiguous effective date (`EFFECTIVE 01 OCT 2026`) and a volume
number, which would be a genuine improvement — left as future work rather than guessed at
now.

## Error handling

- Missing `FAACIFP18` → `InstallationException`. Cannot happen after `recognizes()`, which
  checks the same condition.
- I/O failure → wrapped in `InstallationException`.
- Unparseable ARINC 424 header → coverage warning is simply omitted; installation still
  succeeds, since the header is not needed to install.
- `NavDataManager.reload()` is asynchronous and runs on a background thread; a failure there
  cannot fail an install that already wrote the file.

## Testing

`mvn -B clean test -pl xpman-api` — **144 tests, 0 failures, 10 pre-existing skips**
(was 131 before this feature).

- `CifpInstallableTypeTest` (8): recognition positive/negative including the `FAACIFP18.dat`
  near-miss, ClassGraph discovery, install of both flat and nested entries to `earth_424.dat`,
  absence of leaked folders, and the US-only warning.
- `Arinc424HeaderTest` (4): real HDR records in their true column layout; a commercial
  publisher; a non-ARINC file; an empty file.
- `Arinc424DataSetTest` (5): the overriding flag per instance, the coverage warning, and no
  coverage claim for a commercial publisher or an absent file.
- `NavDataSetTest` (4): added `reportsPresentWhenCycleCannotBeRead`; the existing
  `infoWhenNoData` still passes because an empty set has no files at all.

Verified against a **real** downloaded cycle (`CIFP_261001.zip`, 9.1 MB) outside the suite:
recognized, installed to a 50,317,816-byte `earth_424.dat`, nothing else appearing under
`Custom Data`, and the override layer reporting `US-only coverage (FAACIFP18)`.

## Two ARINC 424 layers, not two names for one

The obvious trap here is reading `earth_424.dat` and `FAACIFP18` as the same mechanism under
two filenames. X-Plane's navdata documentation (titled *"Navdata in X-Plane 11 and 12"*)
describes them as two distinct layers, both live in XP11 and XP12:

| | `earth_424.dat` | `FAACIFP18` |
|---|---|---|
| Layer | Sim-wide ARINC424 override | Updated approaches |
| Loads | fixes, navaids, **airways**, holdings, MSA/MORA, ILS, markers, airports, procedures, runway, comms, path points, GLS, GBAS | terminal/P\* only: terminal fixes, terminal navaids, ILS, markers, airports, gates, procedures, runway, path points |
| Enroute airways / navaids | yes | explicitly no — *"cannot be replaced safely as it would affect the referential integrity of the airway network"* |
| Other layers | **all suppressed** | layered on top, overriding per airport |
| Aimed at | *"professional customers with access to 424 master files"* | FAA's free data, but only with matching-cycle global navdata |

`isOverriding()` is therefore false for the approaches set because it overrides *content
within* the global layers rather than *suppressing* them — not because it is obsolete.

## Why `earth_424.dat` is the install target

The `FAACIFP18` layer only takes effect when its AIRAC cycle matches the underlying global
navdata. X-Plane ships a base layer whose cycle *"will remain the same over the lifetime of
X-Plane 12"*, so a current FAA cycle can essentially never match it — that layer only helps
someone who already runs same-cycle Navigraph or Aerosoft data.

`earth_424.dat` is therefore the only option that works for the free user this feature
targets, and it is what the community actually does on both versions (the 2017 XP11
walkthrough and 2024 XP12 reports both rename `FAACIFP18` to `earth_424.dat`).

The consequence is exactly the warning we show: outside FAA-authority airspace the map goes
empty, and *"if you want to fly outside the USA, you would need to remove or rename
earth_424.dat to enable X-Plane to use the worldwide default navdata"*. A user who already
pays for Navigraph or Aerosoft data will lose it to this override, so the install warning
says so explicitly.

## Out of scope

- Downloading or discovering cycles.
- Installing as `FAACIFP18`. It is a real, supported layer rather than a legacy filename, and
  it is the better choice for a subscriber with same-cycle global data — but XPman cannot
  detect that setup, and it does nothing for the free user. Worth offering later, if ever.
- General partial-coverage detection for non-FAA publishers.
- Extracting an AIRAC 424 effective date or cycle number for display.
- Uninstall (removing `earth_424.dat`) — the wizard installs, it does not uninstall.