# Skunkcrafts Beta Update Channels Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Notify stable and beta Skunkcrafts updates separately with content-based availability (CRC file diff), and let the user choose which channel to install when both offer updates.

**Architecture:** `SkunkcraftsUpdater.computeChannelStatus` fetches each channel's whitelist once, dedups beta by whitelist identity, CRC-diffs both channels and returns a `SkunkcraftsChannelStatus` record (stable/beta availability + beta label). Each domain class caches it via the existing `@Getter(lazy=true)` idiom and every manager pre-warms it off the FX thread. Channel behavior (label, summary, apply) lives in a `SkunkcraftsChannel` enum; the wizard and UI surfaces consume it. The local cfg's `module|` URL is never rewritten.

**Tech Stack:** Java 25, JPMS, Lombok, JavaFX + ControlsFX wizard, JUnit 5 + Hamcrest, JDK built-in `com.sun.net.httpserver.HttpServer` for test fixtures.

**Spec:** `docs/superpowers/specs/2026-10-27-beta-update-channels-design.md`

## Global Constraints

- Java 25, JPMS enforced, **no new dependencies**.
- House style: Lombok (`@Getter(lazy=true)`, `@UtilityClass`), records, interface defaults, polymorphic enums over `if/else`; Javadoc one-liners on classes.
- Version strings are display-only labels; **availability is content (CRC diff) only**.
- Never rewrite the local cfg's `module|` URL; only `version|` via the existing `writeVersion`.
- Manual chapters `02-home-dashboard.md` and `10-updating-addons.md` updated in the same commit as the UI task; verify with `./scripts/build-manual.sh 1.0.1-SNAPSHOT`.
- Single-module tests: `mvn test -pl xpman-api`; full verification: `mvn test`.

## Review Focus

1. **Whitelist unreachable (network down / 404):** both channels report no update, no crash — same tolerance as today's null version. Test: Task 1 `computeChannelStatusToleratesUnreachableWhitelist`.
2. **Beta module URL dead while stable works:** no beta row, stable unaffected. Test: Task 1 `computeChannelStatusIgnoresDeadBetaModule`.
3. **Version-only label bump (openSAM Library):** labels differ, files identical → no offer anywhere, wizard says "Files are up to date — local and remote versions differ". Tests: Task 2 `labelMismatchWithoutContentIsNotAnUpdate`, Task 5 page-1 message logic.
4. **Same-label channels with different content (xa-snow):** beta is offered — labels never suppress. Test: Task 1 `computeChannelStatusOffersSameLabelBetaWhenContentDiffers`.
5. **Disabled/locked addon (incl. SGES in `Scripts (disabled)/`):** status is `NONE` for both channels. Tests: Task 2 `disabledAndLockedAddonsHaveNoChannelStatus`, Task 3 `SgesSkunkcraftsTest.disabledSgesIsNotUpdatable` (kept green).

---

### Task 1: Channel status computation in `SkunkcraftsUpdater` + test server fixture

**Files:**
- Create: `xpman-api/src/main/java/com/ogerardin/xplane/skunkcrafts/SkunkcraftsChannelStatus.java`
- Create: `xpman-api/src/test/java/com/ogerardin/xplane/test/skunkcrafts/SkunkcraftsServerFixture.java`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/skunkcrafts/SkunkcraftsUpdater.java` (constants at :40-43, `findConfig` at :72-83)
- Test: `xpman-api/src/test/java/com/ogerardin/xplane/test/skunkcrafts/SkunkcraftsUpdaterTest.java`

**Interfaces:**
- Consumes: existing `fetchRemoteWhitelist`, `fetchRemoteBlacklist`, `parseLocalIgnore`, `computeFilesToUpdate`, `fetchRemoteVersion`, `computeCRC32`.
- Produces (used by Tasks 2–3):
  - `public record SkunkcraftsChannelStatus(boolean stableUpdateAvailable, boolean betaUpdateAvailable, String betaLatestVersion)` with `public static final SkunkcraftsChannelStatus NONE = new SkunkcraftsChannelStatus(false, false, null);` and `public boolean anyUpdateAvailable()`.
  - `SkunkcraftsUpdater.findBetaConfig(Path folder)` → `SkunkcraftsConfig` or null.
  - `SkunkcraftsUpdater.computeChannelStatus(Path baseFolder, SkunkcraftsConfig stableCfg, SkunkcraftsConfig betaCfg)` → `SkunkcraftsChannelStatus`.
  - `SkunkcraftsServerFixture.start()` / `.baseUrl()` / `.serve(String path, String body)` / `.close()`.

- [ ] **Step 1: Write the failing tests**

Add to `SkunkcraftsUpdaterTest` (uses `@TempDir Path tempDir`, computes real CRCs with the existing `SkunkcraftsUpdater.computeCRC32`):

- `findBetaConfigReturnsNullWhenAbsent` / `findBetaConfigReturnsConfigWhenPresent` — write/omit `skunkcrafts_updater_beta.cfg` in tempDir, assert parse result (module URL + version come back).
- `computeChannelStatusReportsStableOnlyWhenNoBetaCfg` — fixture serves cfg + whitelist with one file whose local CRC mismatches; local file exists; no beta cfg → `status(true, false, null)`.
- `computeChannelStatusSuppressesBetaWhenWhitelistsIdentical` — beta cfg present, beta whitelist byte-identical to stable, local file behind → `status(true, false, null)` (the dedup rule; SGES today).
- `computeChannelStatusOffersBetaWhenContentDiffers` — beta whitelist gives the same file a different CRC → `status(false, true, betaLabelFromBetaCfg)`; make both cfg labels equal to pin the xa-snow same-label case.
- `computeChannelStatusOffersSameLabelBetaWhenContentDiffers` — covered by the assertion above (labels equal, beta still `true`); if kept as one test, assert both facts in it.
- `computeChannelStatusToleratesUnreachableWhitelist` — fixture serves cfgs but no whitelist → `status(false, false, null)`, no exception.
- `computeChannelStatusIgnoresDeadBetaModule` — beta cfg module URL points at the fixture's 404 path → `status(stableDiffResult, false, null)`.

`SkunkcraftsServerFixture`: wrap `HttpServer` (port 0) with a single handler serving `Map<String,String>` by exact path, 404 otherwise, started in `start()`, stopped in `close()` (implements `AutoCloseable`); `serve()` returns `this` for chaining; Javadoc: serves the remote side of the Skunkcrafts protocol (cfg, `_beta.cfg`, whitelists, files) from memory.

- [ ] **Step 2: Run to verify failure**

Run: `mvn test -pl xpman-api -Dtest=SkunkcraftsUpdaterTest`
Expected: compile errors (`SkunkcraftsChannelStatus`/`findBetaConfig`/`computeChannelStatus` missing).

- [ ] **Step 3: Implement**

- `SkunkcraftsChannelStatus` record as in Interfaces; Javadoc: content-truth availability per channel plus the beta channel's latest label.
- `SkunkcraftsUpdater`: add `private static final String BETA_CFG_FILENAME = "skunkcrafts_updater_beta.cfg";`; `findBetaConfig(Path folder)` mirrors `findConfig` with the beta filename.
- `computeChannelStatus(baseFolder, stableCfg, betaCfg)`:
  - Fetch stable whitelist; build stable ignore set (remote blacklist + local ignore + `!stableCfg.liveries()` → `"liveries"`), diff → `stable = diff non-empty`.
  - If `betaCfg == null || betaCfg.moduleUrl() == null` → `new SkunkcraftsChannelStatus(stable, false, null)`.
  - Fetch beta whitelist; if `equals(stableWhitelist)` (records → value equality) → `new SkunkcraftsChannelStatus(stable, false, null)`.
  - Else diff beta (beta's own ignore set) → `beta = diff non-empty`; `betaLatest = beta ? fetchRemoteVersion(betaCfg.moduleUrl()) : null`; return status.
  - Reuse the private `fetchAndDiff` body's pieces but fetch each whitelist once; a `computeFilesToUpdate(baseFolder, whitelist, ignoreSet)` call per channel.
- [ ] **Step 4: Run tests, verify pass**

Run: `mvn test -pl xpman-api -Dtest=SkunkcraftsUpdaterTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add xpman-api/src/main/java/com/ogerardin/xplane/skunkcrafts/SkunkcraftsChannelStatus.java \
  xpman-api/src/main/java/com/ogerardin/xplane/skunkcrafts/SkunkcraftsUpdater.java \
  xpman-api/src/test/java/com/ogerardin/xplane/test/skunkcrafts/
git commit -m "feat: content-based Skunkcrafts channel status computation"
```

---

### Task 2: `SkunkcraftsUpdatable` channel defaults + `SkunkcraftsChannel` enum

**Files:**
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/skunkcrafts/SkunkcraftsUpdatable.java`
- Create: `xpman-api/src/main/java/com/ogerardin/xplane/skunkcrafts/SkunkcraftsChannel.java`
- Test: `xpman-api/src/test/java/com/ogerardin/xplane/test/skunkcrafts/SkunkcraftsUpdatableTest.java`
- Modify (compile fix): `xpman-fx/src/test/java/com/ogerardin/xpman/test/UiFlyWithLuaScriptTest.java` (its anonymous `SkunkcraftsUpdatable` fake must implement the new abstract method)

**Interfaces:**
- Consumes: Task 1's `SkunkcraftsChannelStatus`, `findBetaConfig`, `computeChannelStatus`; existing `getSkunkcraftsUpdateSummary`, `applySkunkcraftsUpdate` idioms.
- Produces (used by Tasks 3–6), all on `SkunkcraftsUpdatable` unless noted:
  - `SkunkcraftsChannelStatus getSkunkcraftsChannelStatus();` — abstract (implemented per domain class in Task 3 via `@Getter(lazy=true)`).
  - `default SkunkcraftsChannelStatus computeSkunkcraftsChannelStatus()` — `isSkunkcraftsUpdatable() ? SkunkcraftsUpdater.computeChannelStatus(getSkunkcraftsFolder(), getSkunkcraftsConfig(), getSkunkcraftsBetaConfig()) : NONE`.
  - `default SkunkcraftsConfig getSkunkcraftsBetaConfig()` → `SkunkcraftsUpdater.findBetaConfig(getSkunkcraftsFolder())`.
  - `default boolean isSkunkcraftsUpdateAvailable()` — **changed**: now `getSkunkcraftsChannelStatus().stableUpdateAvailable()`.
  - `default boolean isSkunkcraftsBetaUpdateAvailable()` / `default boolean isAnySkunkcraftsUpdateAvailable()` / `default String getSkunkcraftsBetaLatestVersion()` — read the status record.
  - `default SkunkcraftsUpdateSummary getSkunkcraftsBetaUpdateSummary()` — empty summary unless updatable with a beta cfg, else `SkunkcraftsUpdater.computeFilesToUpdateSummary(getSkunkcraftsFolder(), getSkunkcraftsBetaConfig())`.
  - `default void applySkunkcraftsBetaUpdate(ProgressListener progress)` — throw `SkunkcraftsUpdateException` when not updatable or no beta cfg; else `SkunkcraftsUpdater.applyUpdate(getSkunkcraftsFolder(), getSkunkcraftsBetaConfig(), progress)`.
  - `public enum SkunkcraftsChannel { STABLE("Stable"), BETA("Beta") }` — constant-specific bodies (polymorphic enum, no if/else): `boolean hasUpdate(SkunkcraftsUpdatable u)`, `String latestVersion(SkunkcraftsUpdatable u)`, `SkunkcraftsUpdateSummary updateSummary(SkunkcraftsUpdatable u)`, `void apply(SkunkcraftsUpdatable u, ProgressListener progress)` (STABLE delegates to the existing stable methods, BETA to the beta ones), `String getLabel()`; plus `public static SkunkcraftsChannel resolve(SkunkcraftsChannel requested, SkunkcraftsUpdatable u)` — requested if `hasUpdate`, else the other channel if it `hasUpdate`, else requested.

- [ ] **Step 1: Write the failing tests**

Rewrite `SkunkcraftsUpdatableTest`'s `FakeAddon`: keep `getName/getVersion/getSkunkcraftsFolder/getSkunkcraftsConfig/getSkunkcraftsLatestVersion`, add `SkunkcraftsChannelStatus getSkunkcraftsChannelStatus()` (constructor-injected). Tests:

- Existing eligibility tests (null/disabled/locked cfg → not updatable) stay, asserting `NONE` status outcomes.
- `labelMismatchWithoutContentIsNotAnUpdate` — installed `"1.0"`, latest `"2.0"`, status `NONE` → `isSkunkcraftsUpdateAvailable()` false, `isAnySkunkcraftsUpdateAvailable()` false (openSAM pin).
- `betaStatusDrivesBetaAvailability` — status `(false, true, "Beta")` → `isSkunkcraftsBetaUpdateAvailable()` true, `getSkunkcraftsBetaLatestVersion()` `"Beta"`, `isAnySkunkcraftsUpdateAvailable()` true.
- `disabledAndLockedAddonsHaveNoChannelStatus` — canned `NONE` for a disabled/locked cfg FakeAddon (content gate lives in `computeSkunkcraftsChannelStatus`, exercised via a subclass overriding it to call the real default).
- `resolveReturnsRequestedWhenItHasAnUpdate` / `resolveFallsBackToSoleAvailableChannel` / `resolveReturnsRequestedWhenNeitherHasUpdate` — `SkunkcraftsChannel.resolve` matrix over FakeAddon statuses.
- Update `UiFlyWithLuaScriptTest`'s fake: implement `getSkunkcraftsChannelStatus()` returning a canned record (keep its existing assertions meaningful).

- [ ] **Step 2: Run to verify failure**

Run: `mvn test -pl xpman-api -Dtest=SkunkcraftsUpdatableTest`
Expected: compile errors on the new methods/enum. Also `mvn test -pl xpman-fx -Dtest=UiFlyWithLuaScriptTest` shows the fake missing the method.

- [ ] **Step 3: Implement** — the interface defaults and enum exactly as in Interfaces; Javadoc one-liners; labels stay display-only.

- [ ] **Step 4: Run tests, verify pass**

Run: `mvn test -pl xpman-api -Dtest=SkunkcraftsUpdatableTest` and `mvn test -pl xpman-fx -Dtest=UiFlyWithLuaScriptTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add xpman-api/src/main/java/com/ogerardin/xplane/skunkcrafts/ \
  xpman-api/src/test/java/com/ogerardin/xplane/test/skunkcrafts/SkunkcraftsUpdatableTest.java \
  xpman-fx/src/test/java/com/ogerardin/xpman/test/UiFlyWithLuaScriptTest.java
git commit -m "feat: SkunkcraftsUpdatable channel defaults and SkunkcraftsChannel enum"
```

---

### Task 3: Domain classes cache the status; managers pre-warm both channels

**Files:**
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/plugins/Plugin.java` (after :105-108)
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/aircraft/Aircraft.java` (after :131-134)
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryPackage.java` (after :113-116)
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/plugins/custom/lua/Sges.java` (after :86-89, plus `ownUpdaterCfg` area :95-98)
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/plugins/PluginManager.java:130-143`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/aircraft/AircraftManager.java:96-105`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryManager.java:101-108`
- Test: `xpman-api/src/test/java/com/ogerardin/xplane/test/plugins/SgesSkunkcraftsTest.java`

**Interfaces:**
- Consumes: Task 2's `computeSkunkcraftsChannelStatus`, `getSkunkcraftsBetaConfig`.
- Produces: every `Plugin`/`Aircraft`/`SceneryPackage`/`Sges` instance carries a cached `getSkunkcraftsChannelStatus()` — what all UI (Tasks 4–6) reads.

- [ ] **Step 1: Write the failing tests**

Rework `SgesSkunkcraftsTest` onto `SkunkcraftsServerFixture` (its private `httpServer` helper can only serve a version cfg; content-based availability needs whitelists + files):

- `outdatedSgesReportsStableUpdate` — fixture serves stable cfg (`version|81.2`), stable whitelist listing the script with a bogus CRC → `isSkunkcraftsUpdateAvailable()` true (was version-based; now content).
- `upToDateSgesReportsNoUpdate` — whitelist CRC computed from the actual installed script file → false.
- `sgesBetaChannelIsOfferedWhenBetaContentDiffers` — install a local `skunkcrafts_updater_beta.cfg` (module → fixture `/beta/`), fixture serves beta cfg + whitelist differing from stable → `isSkunkcraftsBetaUpdateAvailable()` true, `getSkunkcraftsBetaLatestVersion()` = beta cfg label; stable untouched.
- `sgesBetaSuppressedWhenBetaWhitelistIdentical` — beta whitelist identical to stable → false.
- `sgesDoesNotClaimAForeignBetaUpdaterCfg` — local beta cfg belonging to BetterPushback → `getSkunkcraftsBetaConfig()` null, no beta availability (the ownership guard).
- `applyBetaUpdateDownloadsFromBetaModuleAndWritesBetaVersion` — fixture serves a beta-changed file; `applySkunkcraftsBetaUpdate` downloads it into the FlyWithLua folder and rewrites the local `skunkcrafts_updater.cfg` `version|` line to the beta label; the `module|` line unchanged.
- Keep `disabledSgesIsNotUpdatable` and `flyWithLuaPluginDoesNotClaimTheSgesUpdaterCfg` green (NONE status).

- [ ] **Step 2: Run to verify failure**

Run: `mvn test -pl xpman-api -Dtest=SgesSkunkcraftsTest`
Expected: failures — old version-based expectations and missing domain behavior.

- [ ] **Step 3: Implement**

- In each of `Plugin`, `Aircraft`, `SceneryPackage`, `Sges` add (one line + Javadoc):
  `@Getter(lazy = true) private final SkunkcraftsChannelStatus skunkcraftsChannelStatus = computeSkunkcraftsChannelStatus();`
- `Sges`: override `getSkunkcraftsBetaConfig()` mirroring `ownUpdaterCfg`: `SkunkcraftsUpdater.findBetaConfig(getSkunkcraftsFolder())`, null unless `identifiesAsSges(cfg)`.
- `PluginManager.loadPlugins`: in the existing `items.forEach` add `p.getSkunkcraftsChannelStatus();`; in the FlyWithLua script pre-warm loop keep `getSkunkcraftsLatestVersion()` and add `getSkunkcraftsChannelStatus()` (label + content cached before LOADED).
- `AircraftManager.loadAircrafts`: before `fireEvent(LOADED...)` add `items.parallelStream().forEach(a -> { a.getSkunkcraftsLatestVersion(); a.getSkunkcraftsChannelStatus(); });`
- `SceneryManager` load method (the one firing LOADED, after `items = buildEntries();`): `items.stream().map(SceneryEntry::getSceneryPackage).filter(Objects::nonNull).forEach(p -> { p.getSkunkcraftsLatestVersion(); p.getSkunkcraftsChannelStatus(); });` — only in the load path, not the other `buildEntries()` call sites (they are in-memory updates and must not refetch).

- [ ] **Step 4: Run tests, verify pass**

Run: `mvn test -pl xpman-api`
Expected: PASS (Sges + all existing suites; watch for tests hitting real X-Plane installs — they self-skip via `@EnableOn*`).

- [ ] **Step 5: Commit**

```bash
git add xpman-api/src/main/java/ xpman-api/src/test/java/com/ogerardin/xplane/test/plugins/SgesSkunkcraftsTest.java
git commit -m "feat: domain classes cache channel status; managers pre-warm both channels"
```

---

### Task 4: `UpdateAvailableInspection` splits on Skunkcrafts-updatability

**Files:**
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/inspection/impl/UpdateAvailableInspection.java:21-31`
- Test: `xpman-api/src/test/java/com/ogerardin/xplane/test/inspection/UpdateAvailableInspectionTest.java`

**Interfaces:**
- Consumes: Task 2's `isSkunkcraftsUpdateAvailable/isSkunkcraftsBetaUpdateAvailable/getSkunkcraftsBetaLatestVersion`; existing `getLatestVersion/getInstalledVersion`.
- Produces: for `isSkunkcraftsUpdatable()` targets — WARN `Update available: {getSkunkcraftsLatestVersion()}` when stable diff > 0, plus WARN `Beta update available: {getSkunkcraftsBetaLatestVersion()}` when beta offered; for non-Skunkcrafts targets with a custom publication channel (XIvAp, ZiboMod738) — the existing version-based WARN unchanged.

- [ ] **Step 1: Write the failing tests**

In `UpdateAvailableInspectionTest` (its fake target gains a canned `SkunkcraftsChannelStatus`):

- `skunkcraftsTargetEmitsContentBasedMessages` — status `(true, true, "Beta")` → two WARNs with the exact messages above.
- `labelMismatchWithoutContentEmitsNothing` — labels differ, status `NONE` → empty result (openSAM pin at inspection level).
- `nonSkunkcraftsTargetKeepsVersionBasedWarning` — no cfg, `getLatestVersion()` override returning `"2.0"`, installed `"1.0"` → the old WARN `Update available: 2.0` (XIvAp/Zibo preserved).

- [ ] **Step 2: Run to verify failure** — `mvn test -pl xpman-api -Dtest=UpdateAvailableInspectionTest` → new tests fail.

- [ ] **Step 3: Implement** — branch on `target.isSkunkcraftsUpdatable()` in `inspect()`; Skunkcrafts branch reads the status booleans + labels; else branch is the existing version logic verbatim.

- [ ] **Step 4: Run tests, verify pass** — `mvn test -pl xpman-api -Dtest=UpdateAvailableInspectionTest` → PASS.

- [ ] **Step 5: Commit**

```bash
git add xpman-api/src/main/java/com/ogerardin/xplane/inspection/impl/UpdateAvailableInspection.java \
  xpman-api/src/test/java/com/ogerardin/xplane/test/inspection/UpdateAvailableInspectionTest.java
git commit -m "feat: content-based update inspection with separate beta message"
```

---

### Task 5: Wizard carries a channel; page 1 selector + mismatch message

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/install/wizard/SkunkcraftsUpdateWizard.java`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/install/wizard/SkunkcraftsPage1Controller.java`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/install/wizard/SkunkcraftsPage2Controller.java:44`
- Modify: `xpman-fx/src/main/resources/fxml/install_wizard/skunkcrafts_page1.fxml`

**Interfaces:**
- Consumes: Task 2's `SkunkcraftsChannel` (`resolve`, `hasUpdate`, `latestVersion`, `updateSummary`, `apply`).
- Produces: `SkunkcraftsUpdateWizard(SkunkcraftsUpdatable updatable)` (stable, existing call sites unchanged) and `SkunkcraftsUpdateWizard(SkunkcraftsUpdatable updatable, SkunkcraftsChannel requested)`; `getChannel()`/`channelProperty()` (an `ObjectProperty<SkunkcraftsChannel>`, default `STABLE`).

- [ ] **Step 1: Implement the wizard channel plumbing** (JavaFX controllers have no existing automated tests; `resolve` is already pinned in Task 2)

- `SkunkcraftsUpdateWizard`: add the two-arg constructor (one-arg delegates with `STABLE`), `ObjectProperty<SkunkcraftsChannel> channel`, getter + property accessor; title stays "Skunkcrafts Update".
- Page 1 controller `onEnteringPage`: `wizard.setChannel(SkunkcraftsChannel.resolve(wizard.getChannel(), updatable))`; selector (`RadioButton` pair "Stable"/"Beta" in a `ToggleGroup`, new `fx:id channelBox` + radios in the fxml) visible+managed only when both `hasUpdate`; toggle change re-runs the (extracted) async refresh for the new channel. Refresh uses `channel.latestVersion(updatable)` and `channel.updateSummary(updatable)`.
- Mismatch message: when `summary.fileCount() == 0` → `filesCountLabel` shows `"Files are up to date — local and remote versions differ"` if `channelLatest != null && !Objects.equals(updatable.getInstalledVersion(), channelLatest)`, else today's `"Already up to date"`; `invalidProperty` stays true (nothing to install) in both cases.
- Page 2: `updatable.applySkunkcraftsUpdate(...)` → `wizard.getChannel().apply(updatable, this::updateProgress)`.

- [ ] **Step 2: Verify build and existing tests**

Run: `mvn -B -DskipTests clean package` then `mvn test`
Expected: build PASS; all tests PASS.

- [ ] **Step 3: Commit**

```bash
git add xpman-fx/src/main/java/com/ogerardin/xpman/install/wizard/ xpman-fx/src/main/resources/fxml/install_wizard/
git commit -m "feat: Skunkcrafts wizard channel selection and honest up-to-date message"
```

---

### Task 6: UI gates any-channel; Home dashboard rows per channel; manual

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/aircraft/UiAircraft.java:97` (`@EnabledIf`)
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/plugins/UiPlugin.java:39,108`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/UiSceneryEntry.java:39,121`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/plugins/UiFlyWithLuaScript.java:44-49`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/aircraft/AircraftCardView.java:73`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/home/HomeController.java:189-234,260-377`
- Modify: `docs/manual/02-home-dashboard.md`, `docs/manual/10-updating-addons.md`

**Interfaces:**
- Consumes: Task 2's `isAnySkunkcraftsUpdateAvailable`, `isSkunkcraftsBetaUpdateAvailable`, `getSkunkcraftsBetaLatestVersion`; Task 5's wizard two-arg constructor.
- Produces: user-visible behavior — every update entry point opens a wizard that lands on an installable channel; Home shows up to two rows per addon.

- [ ] **Step 1: Write the failing test**

`UiFlyWithLuaScriptTest`: script fake with canned status `(true, true, "Beta")` → `UiFlyWithLuaScript.updateAvailable()` true, `getLatestVersion()` still the stable label; fake with `NONE` → false.

- [ ] **Step 2: Run to verify failure** — `mvn test -pl xpman-fx -Dtest=UiFlyWithLuaScriptTest` → fails (old `isSkunkcraftsUpdateAvailable` wiring).

- [ ] **Step 3: Implement**

- SpEL gates: `UiAircraft:97` and `UiPlugin:108` → `@EnabledIf("anySkunkcraftsUpdateAvailable")`; `UiSceneryEntry:121` → `@EnabledIf("sceneryPackage != null && sceneryPackage.anySkunkcraftsUpdateAvailable")` (Lombok `@Delegate` forwards the new default method, same mechanism as today).
- Property getters: `UiPlugin:39`, `UiSceneryEntry:39`, `UiFlyWithLuaScript:44` → `isAnySkunkcraftsUpdateAvailable()`; `AircraftCardView:73` → `uiAircraft.isAnySkunkcraftsUpdateAvailable()` (card button keeps opening the channel-less wizard — resolution handles it).
- `HomeController`: the five filter chains (`checkAircraftUpdates`, `checkSceneryUpdates`, `checkPluginUpdates`, `updatablePluginAddons`, `updateUpdatesSection`) → single `.filter(SkunkcraftsUpdatable::isAnySkunkcraftsUpdateAvailable)`. In `createCategorySection`, per addon emit a stable row (`getSkunkcraftsLatestVersion()`, button `"Update"`, wizard `STABLE`) when `isSkunkcraftsUpdateAvailable()`, and a beta row (`getSkunkcraftsBetaLatestVersion()`, button `"Update Beta"`, wizard `BETA`) when `isSkunkcraftsBetaUpdateAvailable()`; keep the manager-reload-after-wizard block per row action.
- Version cell factories need no changes (they read the Ui* properties updated above).
- Manual: `02-home-dashboard.md` — describe the two rows, badge meaning "any channel", counts by addon; `10-updating-addons.md` — channel selector in the wizard, "Files are up to date — local and remote versions differ" state, episodic switching (stable row as the way back), whitelist-identity dedup.

- [ ] **Step 4: Run tests + build the manual**

Run: `mvn test` and `./scripts/build-manual.sh 1.0.1-SNAPSHOT`
Expected: PASS; PDF builds.

- [ ] **Step 5: Commit (feature + manual in one commit per AGENTS.md)**

```bash
git add xpman-fx/src/ docs/manual/02-home-dashboard.md docs/manual/10-updating-addons.md
git commit -m "feat: beta update channels in UI with manual updates"
```

---

### Task 7: Full verification

- [ ] **Step 1:** `mvn -B -DskipTests clean package` — full build green.
- [ ] **Step 2:** `mvn test` — both test modules green (local X-Plane gated tests self-skip).
- [ ] **Step 3:** Confirm the manual PDF built in Task 6 is current; `git status` clean.
