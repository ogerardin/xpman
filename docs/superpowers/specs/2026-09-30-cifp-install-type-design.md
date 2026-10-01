# FAA CIFP (ARINC 424) Installable Type

**Date:** 2026-09-30 (rewritten 2026-10-01 after implementation verification)
**Status:** Implemented

## Goal

Let XPman install free FAA CIFP (ARINC 424) navigation data. The user drops the FAA cycle
ZIP on the existing install wizard; XPman extracts `FAACIFP18` and writes it into
`Custom Data` under its own name, which is the slot X-Plane documents for this file: it
overrides approach, airport and terminal data for US airports on top of whatever global
navdata is installed, and leaves the rest of the world alone.

The install warns that only US airports are affected and that X-Plane applies the file only
when the AIRAC cycles match. The navdata panel then reports whether the layer is actually in
use, and warns that the FAA dataset covers the US only.

## Motivations

- FAA CIFP is free, current-cycle (28-day), and needs no Navigraph subscription. The
  manual procedure is small but non-obvious: download the current cycle, extract it, find the
  extensionless `FAACIFP18` among the PDFs, and drop it into `Custom Data` under its own name.
- XPman already models `FAACIFP18` as the *FAA updated approaches* navdata layer
  (`NavDataManager.faaUpdatedApproaches()`) and reports on it, but offered no way to install one.
- The wizard is fully generic: `Page2Controller` builds a `GenericInstaller` over an
  `ArchiveInstallSource`, which resolves the concrete type by ClassGraph discovery. A new
  `InstallableType` is picked up with **zero UI changes**. `NavDataController.install()`
  already launches that wizard.

## Decisions

| Decision | Choice |
|---|---|
| Input | User-selected FAA cycle ZIP from `https://www.faa.gov/air_traffic/flight_info/aeronav/digital_products/cifp/download/`. XPman never discovers or downloads the cycle |
| Recognition | Archive contains an entry whose **file name is exactly `FAACIFP18`** (FAA ships it extensionless). No overlap with `NavDataInstallableType`, which matches `earth_*.dat` |
| Install target | `Custom Data/FAACIFP18` — the layer X-Plane documents for this file. Never `earth_424.dat`, which is for *global* 424 masters |
| Conversion | **None.** See "No converter" below |
| Coverage warning | `Severity.WARN` in `preconditions()`, so wizard page 2 shows it before the user commits; only `ERROR` blocks the Next button |
| Coverage detection | ARINC 424 `HDR01` record, cols 6–15 carry the dataset's own name. FAA datasets are named `FAACIFP*` |
| Cycle detection | ARINC 424 `HDR04`, labelled `VOLUME nnnn` — the same AIRAC designator the XPNAV `data cycle` header uses |
| Cycle consistency | Only the approaches layer: it must match the cycle of the layer it overrides, per X-Plane's documentation. The override replaces the global database and has no such requirement |
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
to `Custom Data/FAACIFP18`, and reloads the navdata manager. The filter is also what keeps the
archive's three readme PDFs, `IN_CIFP.txt` and `Not_In_CIFP.xlsx` out of `Custom Data`.

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

## AIRAC cycles

The cycle of an ARINC 424 file is read from its `HDR04` record's labelled token rather than a
hard-coded column, since the record is self-describing:

```
CODED INSTRUMENT FLIGHT PROCEDURES VOLUME 2610  EFFECTIVE 01 OCT 2026
```

That number is the same AIRAC designator the XPNAV `data cycle` header uses, which is not an
assumption: running X-Plane's own converter over this very file emits `data cycle 2610`, so
cycles read from an ARINC 424 layer and from an XPNAV layer compare directly. The override
layer therefore reports `OK — cycle 2610` instead of an unknown cycle, and
`NavDataSet.inspect()`'s pre-existing within-set mixed-cycle detection starts working for
ARINC 424.

`NavDataItem.normalizeCycle` reduces a value to its trailing four digits, so a cycle written
as `YYYYMM` compares equal to the same cycle as `YYMM`.

**Known caveat:** `DatFileParser.Cycle()` accepts exactly four digits (`repeat(4,4)`), so a
hypothetical six-digit `data cycle 202610` would read as `2026`. Left alone deliberately —
no such file was observed (the converter emits four digits, and AIRAC designators are four
digits), and a navdata severity is cosmetic: only the *install* wizard blocks on `ERROR`.
Hardening it means changing how an existing shared parser consumes digits.

## Cycle consistency

X-Plane documents exactly one cross-layer cycle requirement, and it belongs to the
approaches layer rather than the override:

> *"for integrity reasons, the cycle number of the FAA data must always match the cycle
> number of the underlying layer. Terminal procedures do reference waypoints out of the
> terminal area, therefore, the data source for global waypoints must be at the same cycle
> number."*

The sim-wide override has no such requirement — it replaces the global database rather than
composing with it — so it is never checked.

`NavDataSet.getConsistentCycle()` reports the single cycle a set's files agree on. The check
lives on `Arinc424DataSet` behind a `consistencyMessage()` hook, and resolves the layer it
would be applied to through two shadowing-aware lookups on `NavDataManager`:

| Situation | Severity |
|---|---|
| Sim-wide override installed, so nothing else is read | `WARN` — this layer is not used |
| Effective global layer's cycle known and different | `WARN` — cycle mismatch |
| Otherwise | silent |

Shadowing is what makes this correct rather than a naive "all present layers must agree": the
base layer X-Plane ships is always present, on a cycle of its own that never changes, so every
subscriber would otherwise see a false conflict. `getEffectiveGlobalDataSet()` prefers the
updated base layer and ignores the shipped one when it is shadowed. `Arinc424DataSet.Role`
distinguishes the two roles, since both layers are the same class and the reason one is not
"overriding" is that it composes with the lower layers instead of suppressing them.

## Error handling

- Missing `FAACIFP18` → `InstallationException`. Cannot happen after `recognizes()`, which
  checks the same condition.
- I/O failure → wrapped in `InstallationException`.
- Unparseable ARINC 424 header → coverage warning is simply omitted; installation still
  succeeds, since the header is not needed to install.
- `NavDataManager.reload()` is asynchronous and runs on a background thread; a failure there
  cannot fail an install that already wrote the file.

## Testing

`mvn -B clean test` — **155 tests in `xpman-api`, 0 failures, 10 pre-existing skips**
(was 131 before this feature).

- `CifpInstallableTypeTest` (8): recognition positive/negative including the `FAACIFP18.dat`
  near-miss, ClassGraph discovery, install of both flat and nested entries to `Custom Data/FAACIFP18`,
  absence of leaked folders, and the US-only warning.
- `Arinc424HeaderTest` (5): real HDR records in their true column layout; the `VOLUME 2610`
  cycle; a header with no `VOLUME`; a commercial publisher; a non-ARINC file; an empty file.
- `Arinc424DataSetTest` (11): the role flag, coverage warning, cycle read from the header, and
  the consistency check against a stubbed `NavDataManager` — mismatch, match, updated base
  preferred over the shadowed shipped base, suppressed by the override, and silence in each
  of the negative cases.
- `NavDataSetTest` (8): added `getConsistentCycle()` single/mixed/absent and `normalizeCycle`;
  the existing `infoWhenNoData` still passes because an empty set has no files at all.

Verified against a **real** downloaded cycle (`CIFP_261001.zip`, 9.1 MB) outside the suite:
recognized, and installed to a 53,187,816-byte `Custom Data/FAACIFP18` — with
`Custom Data` containing *exactly* that one file afterwards: no `earth_424.dat`, no PDF, no
spreadsheet. The layer reports `OK — cycle 2610` together with `US-only coverage (FAACIFP18)`
in 3 ms.

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

## Why `FAACIFP18`, and never `earth_424.dat`

The two filenames are not interchangeable. `earth_424.dat` is documented for *"professional
customers with access to 424 master files"* — a **global** master — and once X-Plane reads it
it loads no other navdata text file at all. The FAA publishes US-only cycles: its download
page offers `CIFP 260903` and `261001`, with no world variant. So installing the FAA file as
`earth_424.dat` puts a partial dataset in the global slot and deletes navdata for the rest of
the world — which is why every tutorial that does it is caveated *"THIS IS ONLY FOR USERS WHO
ONLY FLY X-Plane IN THE USA"*.

`FAACIFP18` is the slot built for this exact file, and it composes: it overrides terminal data
for US airports while everything outside the US keeps coming from the installed global layers.

The trade-off is the cycle requirement, which the docs state explicitly for this layer only:

> *"for integrity reasons, the cycle number of the FAA data must always match the cycle number
> of the underlying layer. Terminal procedures do reference waypoints out of the terminal area,
> therefore, the data source for global waypoints must be at the same cycle number."*

X-Plane ships a base layer whose cycle *"will remain the same over the lifetime of X-Plane
12"*, so a current FAA cycle cannot match it. The honest consequence: **for a user on the
shipped base data this file does not take effect.** It applies once same-cycle third-party
navdata is installed, which is the correct outcome — the file is not silently reshaped to
destroy the rest of the world's data in order to appear to work.

That is what the warnings carry. `preconditions()` always states that only US airports are
affected and that the cycles must match, and adds a second warning when
`Custom Data/earth_424.dat` is present, since X-Plane would ignore this file entirely. After
install, `consistencyMessage()` gives the verdict on the panel: cycle mismatch, or "not used"
while an override is installed.

## Out of scope

- Downloading or discovering cycles.
- General partial-coverage detection for non-FAA publishers.
- Extracting the ARINC 424 effective date for display. The cycle is read; the date is not.
- Uninstall (removing `Custom Data/FAACIFP18`) — the wizard installs, it does not uninstall.
- Extracting the readme PDFs and spreadsheet the FAA archive also carries. They stay out of `Custom Data`:
  only the `FAACIFP18` entry is extracted, so the three PDFs, `IN_CIFP.txt` and `Not_In_CIFP.xlsx`
  never land there. The user still has them in the ZIP they downloaded.