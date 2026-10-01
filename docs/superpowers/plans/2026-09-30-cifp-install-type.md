# FAA CIFP Installable Type — Implementation Record

**Date:** 2026-09-30 (plan), 2026-10-01 (rewritten after verification)
**Status:** Superseded in part. See the design spec for the current architecture:
[`../specs/2026-09-30-cifp-install-type-design.md`](../specs/2026-09-30-cifp-install-type-design.md)

This file originally held the task-by-task plan for running `convert424toxplane`. Manual
verification during task 5 disproved that approach, so it has been replaced with a record of
what was planned, what was wrong, and what shipped. The technical detail now lives in the
design spec; keeping two copies here would just be a second thing to go stale.

## What was originally planned

1. Export `install.types` to the test module; add a failing recognition test.
2. Add `Platform.cifpConverterFolder()` / `cifpConverterName(XPlaneMajorVersion)` hooks.
3. Add `CifpInstallableType` matching `FAACIFP18`, delegating to a manager.
4. Add `CifpManager`: download the converter from a Dropbox folder URL, cache it under
   `XPlane/Resources/tools/<os>/`, run it over the source, copy the generated `CIFP/` folder
   into `Custom Data`, and always install the source as `earth_424.dat`.
5. Verify manually against the real converter ZIP and a real FAA cycle.
6. Correct the design doc.

Steps 1–4 shipped in commits `cfd35f5`, `1f21ce4`, `293c62b` and were then largely undone.

## What the verification found

### The converter was solving a problem that does not exist

X-Plane's own navdata documentation states that once `earth_424.dat` is present,
*"X-Plane will not load any other information from other text files"* — which is precisely
the `earth_nav/fix/awy.dat` and `CIFP/` the converter produces. The converter's author
names it a tool for **data providers** publishing X-Plane-native navdata, not an end-user
installer, and its real signature wants an `apt.dat` we do not have. Its output is also
US-only, so copying it into `Custom Data` would replace the global layer and delete
navdata for the rest of the world.

### Two factual errors in the plan

Both were assumptions made before anything was downloaded, and both would have shipped as
bugs:

- **The converter archive has no unknown top-level prefix.** The plan assumed a Dropbox
  wrapper folder that had to be located and stripped. The real ZIP has `mac/`, `linux/` and
  `windows/` at the root. (`findOsRoot` had handled both cases, so no code was wrong — but
  the rationale was.)
- **`geoids/` is required on macOS and Linux too, not just Windows.** The plan noted that
  the archive ships `geoids/` only under `windows/` and inferred it was therefore a Windows
  concern. Running the actual macOS binary failed with
  `Required file missing: geoids/egm96-5.pgm`. Copying the `windows/geoids` folder next to
  the binary made it exit 0. The plan would have shipped a feature broken on two of three
  platforms.

### Also found, not planned for

- The macOS converter binary is a universal Mach-O binary (x86_64 + arm64), so no
  architecture branch was needed.
- `ZipArchive.extract` does not preserve POSIX executable permissions; the macOS binary
  needed a manual `chmod +x` before it would run.
- `DatFileParser` cannot parse ARINC 424 at all, so an installed CIFP file was
  reported as *"No data present"*. Worse, `XPlaneFile` reads the whole URI into a `String`
  before parsing, so the panel was pulling a 50 MB file into memory to look at a header.
- Both are fixed: `Arinc424Header` reads five lines (55 ms on a 50 MB file) and
  `Arinc424NavDataFile` bypasses `DatFile` entirely.

## What shipped instead

| Concern | Implementation |
|---|---|
| Install | `CifpInstallableType` alone: extract the one `FAACIFP18` entry to a temp dir, copy to `Custom Data/FAACIFP18`, reload. 84 lines |
| US-only warning | `Severity.WARN` from `preconditions()`, surfaced by wizard page 2 |
| Overriding layer | `NavDataSet.isOverriding()`, per-instance on `Arinc424DataSet` |
| Coverage detection | `Arinc424Header` reads `HDR01` cols 6–15; `FAACIFP*` means FAA means US-only |
| Panel | Overriding card badged `overrides N layers`; cards below dimmed and badged `ignored by layer N` |
| Status line | Renders the most severe inspection message instead of assuming the summary is last |

**Net: 409 production lines, 319 test lines, 144 tests passing (was 131), 10 pre-existing
skips.** Against the converter branch this is 47 net production lines more — the cost of the
panel and coverage work that replaced 233 lines of converter machinery.

## Cycle reporting and consistency

Added after the first cut shipped, once it was clear that cycle information was both
available and needed.

**Cycles are directly comparable.** The FAA header's `VOLUME 2610` is the same AIRAC
designator the XPNAV `data cycle` header uses — not an assumption: running X-Plane's own
converter over that exact file emits `data cycle 2610`. So `Arinc424Header` reads the
`VOLUME` token from HDR04, the override layer reports `OK — cycle 2610`, and
`NavDataSet.inspect()`'s existing mixed-cycle detection starts working for ARINC 424.
`NavDataItem.normalizeCycle` reduces a value to its trailing four digits so `YYYYMM` and
`YYMM` forms compare equal.

**The cycle-match requirement belongs to `FAACIFP18`, not `earth_424.dat`.** The
documentation states it only for the approaches layer, which composes with the global layers
and therefore needs a matching cycle; the sim-wide override replaces the global database and
has no such requirement. So the assertion is scoped to the approaches role only.

**Shadowing is the subtle part.** The base layer X-Plane ships is always present, on a cycle
of its own that never changes. Comparing every present layer would therefore report a
conflict for every subscriber who installed fresher navdata. `NavDataManager` exposes
`getEffectiveGlobalDataSet()`, which returns the updated base layer when installed and only
falls back to the shipped base otherwise — and returns nothing at all while an override
suppresses the rest, which is the "this layer is not used" case.

**Left alone deliberately:** `DatFileParser.Cycle()` accepts exactly four digits, so a
hypothetical six-digit cycle would read as `2026`. No such file was observed, and a navdata
severity is cosmetic — only the install wizard blocks on `ERROR`. Recorded as a caveat rather
than changing how a shared parser consumes digits.

**Reused instead of invented:** `boolean overriding` became an `Arinc424DataSet.Role` enum,
because both ARINC 424 layers are the same class and the flag was starting to stand for two
different things. The layer names became public constants so `NavDataSetCardView`'s icon map
stops hard-coding strings that have to be kept in step by hand — a drift that already
required a manual fix once in this branch.

## Deliberately not done

- **General partial-coverage detection for non-FAA publishers.** Region identifiers sit in
  per-record-type fixed-width fields and read `UNKUNK` even in FAA data, so this needs
  per-record-type parsing and a maintained region list over a 50 MB file.
- **Extracting the ARINC 424 effective date or cycle number.** `EFFECTIVE 01 OCT 2026` and a
  volume number are both present in the header and unambiguous; the panel currently reports
  the cycle as unknown. Reasonable next increment, not needed for this feature.
- **Uninstall.** The wizard installs; it does not uninstall.