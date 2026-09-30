# FAA CIFP (ARINC 424) Installable Type

**Date:** 2026-09-30
**Status:** Design (approved)

## Goal

Let XPman install free FAA CIFP (ARINC 424) navigation data. The user drops the FAA
cycle ZIP on the existing install wizard; XPman extracts `FAACIFP18`, runs
`convert424toxplane` to produce the X-Plane-specific data set, and writes the result
into `Custom Data` as `earth_424.dat` plus the generated `CIFP` folder.

## Motivations

- FAA CIFP is free, current-cycle (28-day), and needs no Navigraph subscription, but the
  manual procedure (see the X-Plane.org walkthrough) is ~8 non-obvious steps: find the
  current cycle, rename `FAACIFP18` to `.dat`, run a third-party converter with a magic
  `"FMS"` argument, then copy several generated files into the right `Custom Data`
  subfolder and rename the source to `earth_424.dat`.
- XPman already owns the `Custom Data` layout and the inspection/reporting UI, and already
  runs third-party tools during install (ToolsFX). Driving the converter from the install
  wizard removes every manual step.
- The wizard is fully generic: `Page2Controller` builds a `GenericInstaller` over an
  `ArchiveInstallSource`, which resolves the concrete type by ClassGraph discovery. A new
  `InstallableType` is therefore picked up with **zero UI changes**.

## Decisions

| Decision | Choice |
|---|---|
| Input | User-selected FAA cycle ZIP from `https://www.faa.gov/air_traffic/flight_info/aeronav/digital_products/cifp/download/`. XPman never discovers or downloads the cycle |
| Recognition | Archive contains an entry whose **file name is exactly `FAACIFP18`** (FAA ships it extensionless). No overlap with `NavDataInstallableType`, which matches `earth_*.dat` |
| Converter acquisition | Downloaded at install time from Philipp Münzel's public Dropbox folder, cached in `XPlane/Resources/tools` (`XplanePaths.tools()`), reused on later runs |
| Cache location | `XPlane/Resources/tools/<os>/…`, so it survives both re-installs and X-Plane directory re-creation. This is the folder `ToolUtils` already targets for external tools |
| Windows converter variant | XP11 → `convert424toxplane11.exe`; XP12 → `convert424toxplane.exe`, selected via the existing `XPlaneMajorVersion` |
| Conversion failure | Non-zero exit is **not** fatal: log/warn with the converter's stdout+stderr, then continue and still install `FAACIFP18.dat` as `earth_424.dat` |
| Partial install | `CIFP/**` is copied **only** when conversion succeeded; `earth_424.dat` is always installed |
| New files | One class `CifpInstallableType` + one test. No changes to the wizard, FXML, or existing install types |
| Utilities | Reuse `ToolUtils.installFromZip` (download → temp → selective extract), `CommandExecutor` (process + output capture), `Platform` (OS detection, runnability, macOS quarantine) |

### Converter resolution

Under `xPlane.getPaths().tools()`:

| Platform | Entries required |
|---|---|
| Windows, X-Plane 11 | `windows/convert424toxplane11.exe` |
| Windows, X-Plane 12 | `windows/convert424toxplane.exe` and `windows/geoids/**` |
| macOS | `mac/convert424toxplane` |
| Linux | `linux/convert424toxplane` |

`geoids` ships only under `windows/` and must stay adjacent to the executable, so on
Windows the whole `windows/` subtree is extracted as one unit.

## Components

### `CifpInstallableType` (`com.ogerardin.xplane.install.types`)

Public no-arg constructor (required by `IntrospectionHelper.findAllSubclasses`). Mirrors
`NavDataInstallableType`'s shape: matches on `Archive`, delegates filesystem work to a
dedicated manager (below) rather than doing it inline.

- `String description()` → `"FAA CIFP (ARINC 424)"`
- `boolean recognizes(Archive archive)` → true if any entry's file name equals `FAACIFP18`
- `InspectionResult preconditions(XPlane xPlane, Archive archive)` → empty result. Nothing
  can be validated before install, because the converter is fetched on demand and X-Plane
  paths are only read during the install itself
- `void install(XPlane xPlane, Archive archive, ProgressListener progress)` → delegates to
  `CifpManager.install(...)`, wrapping any `IOException`/`InterruptedException` in
  `InstallationException`

`xpman-fx` requires no change: `IntrospectionHelper` scans `com.ogerardin.xplane.**` and the
wizard resolves the type at runtime.

### `CifpManager` (`com.ogerardin.xplane.navdata`)

Owns the filesystem work so the install type stays a thin adapter, matching how
`NavDataInstallableType` delegates to `NavDataManager`.

**Converter resolution**

1. `Path toolsFolder = xPlane.getPaths().tools()`
2. Expected entry = the platform/version row above. If `Files.isExecutable(...)`, reuse it.
3. Otherwise download once and extract:
   - Source is the Dropbox folder root, fetched as a single ZIP by appending `dl=1`:
     `https://www.dropbox.com/scl/fo/mnw9cufqcxgmkzpx35269/AG84gKEZWlR1Sk5Vld0csGk?rlkey=udqtjnhsdo0c7cnhbe2o0ft6o&dl=1`
   - The ZIP nests under an unknown top-level directory name, so the extraction root is
     **resolved by scanning entry names for one ending in `<os>/convert424toxplane*`** and
     using that entry's parent directory as the filter root passed to
     `ToolUtils.installFromZip`. This avoids hardcoding Dropbox's internal prefix.
   - Extracting the whole `<os>` subtree keeps `geoids` alongside the Windows executable.
4. macOS only: if `platform.isQuarantined(binary)`, call `platform.removeQuarantine(binary)`.
5. Verify `platform.isRunnable(binary)`; if false after download, report an error for that
   platform/version combination rather than running a known-broken binary.

### Install flow

1. Create a temporary working directory.
2. Extract the single `FAACIFP18` entry into it; rename to `FAACIFP18.dat`.
3. Resolve the converter as above.
4. Run via `CommandExecutor.builder()`, piping stdout and stderr to the progress listener:
   `<binary> FAACIFP18.dat "FMS"` with `dir` set to the working directory.
5. On non-zero exit, emit a **warning** containing the exit value and captured output, then
   continue to step 7.
6. On success, copy `CIFP/**` from the working directory into
   `xPlane.getPaths().customData().resolve("CIFP")`, overwriting existing entries.
7. Copy `FAACIFP18.dat` to `xPlane.getPaths().customData().resolve("earth_424.dat")`.
8. `xPlane.getNavDataManager().reload()`.
9. Delete the working directory in a `finally` block.

`earth_424.dat` is already the `simWideOverride()` layer in `NavDataManager`, so the
installed file is immediately visible in the redesigned navdata screen.

## Error handling

`InstallableType.install` declares `throws InstallationException`, and
`GenericInstaller.install` propagates it. That is the **abort** channel.
`ProgressListener` has no severity channel (only `progress(ratio, message)` and
`output(message)`), so **non-fatal** problems are reported through the class logger
(`@Slf4j`) *and* mirrored to `progress.output(...)`.

| Condition | Behaviour |
|---|---|
| Converter exits non-zero | **Non-fatal.** `log.warn` + `progress.output` with the exit value and captured stdout/stderr; `CIFP/` skipped; `earth_424.dat` still installed |
| Converter cannot be downloaded | **Abort** — `InstallationException`. This is an environment/network failure, not a data-conversion outcome, and the user would otherwise silently get no FMS data |
| Downloaded binary not runnable | **Abort** — `InstallationException` naming the platform and X-Plane version (signals a wrong converter variant) |
| `FAACIFP18` missing from archive | **Abort** — `InstallationException`. Cannot happen via the wizard because `recognizes()` gates it; still guarded so `install()` is safe to call directly |
| `CIFP/` not produced despite a zero exit | **Non-fatal** — `log.warn` + `progress.output`; `earth_424.dat` still installed |

Downloading and executing a third-party binary is a trust boundary, so the download is
restricted to the single hardcoded HTTPS URL, reuses the existing
`FileUtils.copyURLToFile` path, and its output is never silently discarded — every process
failure reaches the user through the logger and the progress listener.

## Testing

`recognizes()` is the only pure, platform-independent logic, so it is the only part that
gets a real test.

`CifpInstallableTypeTest` — one test method, two assertions against in-memory ZIPs:

- a ZIP containing an entry named `FAACIFP18` → `recognizes()` is true
- a ZIP containing an `earth_424.dat` entry → `recognizes()` is false (guards against
  overlapping `NavDataInstallableType`)

Everything else (download, converter execution, copy) needs a live X-Plane and a real
network, matching how the existing `InstallableType` implementations are verified. Per the
repository's testing conventions it will be exercised manually against a local X-Plane
install, annotated `@EnableOnLocalXPlane` where useful.

## Verification to perform during implementation

These are concrete checks, not deferred design questions:

1. **Dropbox ZIP layout** — fetch the folder once and log the entry names. Confirms the
   `<os>/convert424toxplane*` suffix rule and whether `geoids` sits under `windows/`.
2. **Converter output shape** — run the mac binary on a sample `FAACIFP18.dat` and list the
   working directory. Confirms that `CIFP/` is written into the working directory (step 6
   assumes it is) and records the exact generated file names.
3. **macOS binary architecture** — check whether `mac/convert424toxplane` is a universal
   binary. If it is arm64-only, Intel Macs need a separate path and `Platform.getCpuType()`
   becomes a factor in converter resolution.

If (2) shows the converter does not write `CIFP/` into the working directory, step 6 is
adjusted to point at the actual output location; nothing else in the design changes.

## Files

| Action | File |
|---|---|
| **Create** | `xpman-api/.../install/types/CifpInstallableType.java` — `InstallableType` adapter: `recognizes()` on `FAACIFP18`, empty `preconditions()`, delegates `install()` |
| **Create** | `xpman-api/.../navdata/CifpManager.java` — converter resolution, download, execution, and `Custom Data` copy |
| **Modify** | `xpman-api/.../XPlane.java` — add `private final CifpManager cifpManager = new CifpManager(this);` beside `navDataManager` (line 49), matching the existing manager-ownership pattern |
| **Create** | `xpman-api/src/test/java/com/ogerardin/xplane/test/install/CifpInstallableTypeTest.java` — recognition test over in-memory ZIPs |

No `xpman-fx` changes: the wizard resolves install types at runtime via
`IntrospectionHelper`, so no FXML, controller, or registration edits are required.

## Out of scope

- Downloading or version-checking the FAA cycle itself — the user supplies the ZIP.
- Navigraph subscriptions (already handled by `NavigraphCycleVersion`).
- Caching the *converted output* — only the converter binary is cached; conversion runs on
  every install so a newer FAA cycle always produces fresh data.
- Any wizard, FXML, or `xpman-fx` change.