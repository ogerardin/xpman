# Scenery ini-item strip (pure syntax type) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `SceneryPackIniItem` a pure syntax type (one line of scenery_packs.ini) by deleting the sealed hierarchy and moving all domain resolution policy to `SceneryEntry`; no layer cycle, no FX changes.

**Architecture:** `file.data.scenery` keeps only syntax knowledge (value + disabled + `isToken()` regex + `GLOBAL_AIRPORTS_*` constants). `SceneryEntry` (domain) gains a static `resolveFolder(item, baseFolder, globalAirportsFolder)`. `SceneryManager.buildEntry` calls the static. Parser, data holder, ini-file writer, and all FX code are untouched — the `of(String[, boolean])` factories, `getIniValue()`, `isDisabled()`, `getIniItem()` surface are preserved.

**Tech Stack:** Java 25, Lombok, JUnit 5 + Hamcrest, petitparser.

**Spec:** No dedicated spec; design agreed in session (2026-10-15/16). Predecessor design: `docs/superpowers/specs/2026-08-30-scenery-ini-driven-design.md`.

**Decision record:** "Strip, don't merge" — chosen over merging `SceneryPackIniItem` into `SceneryEntry` to avoid a `scenery ↔ file` package cycle and FX churn. The merge's entry-identity-stability bonus is deliberately foregone; today's item aliasing (entry ↔ manager list) stays, is tested, and works.

## Global Constraints

- Zero user-visible behavior change (all columns, context menus, statuses preserved) → no manual update expected; verify with a grep of `docs/manual/`.
- Keep getter-style accessors (`getIniValue()`, `isDisabled()`) — FX SpEL (`iniItem.disabled`) resolves through them.
- Never commit the user's unrelated files (their dirty work was committed separately as 3975ba9 before this task started).
- Repo build/test commands per AGENTS.md: `mvn test -pl xpman-api`, `mvn -B -DskipTests clean package`.

## Review Focus

1. `resolveFolder` static placement on `SceneryEntry` — pinned by 3 moved unit tests (path resolution, Global Airports token, unknown token → null); no X-Plane install needed.
2. Parser equality relies on `(iniValue, disabled)` — pinned by the 3 parser suites (`SceneryIniFile11/12/DisabledParserTest`).
3. `SceneryManagerTest` token filter becomes `isToken()` (was `instanceof TokenSceneryPackIniItem`) — same semantics, gated on local X-Plane.
4. FX untouched — `getIniItem()`/SpEL `iniItem.disabled` surface preserved; only runtime-verifiable (no FX tests exist), same expressions as today.

---

### Task 1: Flatten `SceneryPackIniItem` to a pure syntax type

**Files:**
- Rewrite: `xpman-api/src/main/java/com/ogerardin/xplane/file/data/scenery/SceneryPackIniItem.java`
- Delete: `xpman-api/src/main/java/com/ogerardin/xplane/file/data/scenery/PathSceneryPackIniItem.java`
- Delete: `xpman-api/src/main/java/com/ogerardin/xplane/file/data/scenery/TokenSceneryPackIniItem.java`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryEntry.java` (add static `resolveFolder`, import `java.nio.file.Path`)
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryManager.java:129` (`item.resolveFolder(...)` → `SceneryEntry.resolveFolder(item, ...)`)
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/scenery/GlobalAirportsSceneryPackage.java` (`TokenSceneryPackIniItem.GLOBAL_AIRPORTS_FOLDER` → `SceneryPackIniItem.GLOBAL_AIRPORTS_FOLDER`)
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryPackage.java` (2 Javadoc lines: `rank`/`enabled` are derived stamps from the ini entry)
- Test: `xpman-api/src/test/java/com/ogerardin/xplane/test/file/SceneryPackIniItemTest.java` (rewrite: `of`/equality/`isToken`; drop `resolveFolder` tests)
- Test: `xpman-api/src/test/java/com/ogerardin/xplane/test/scenery/SceneryEntryTest.java` (+3 `resolveFolder` tests via the static; `new TokenSceneryPackIniItem(...)` → `SceneryPackIniItem.of(...)`)
- Test: `xpman-api/src/test/java/com/ogerardin/xplane/test/petitparser/SceneryIniFile{11,12}ParserTest.java`, `SceneryIniFileDisabledParserTest.java` (subclass ctors → `SceneryPackIniItem.of(v[, d])`)
- Test: `xpman-api/src/test/java/com/ogerardin/xplane/test/scenery/SceneryManagerTest.java` (`instanceof TokenSceneryPackIniItem` → `isToken()`)
- Untouched: `SceneryPacksIniParser.java` (both `of` factories kept), `SceneryPacksIniFile.java`, `SceneryPackIniData.java`, `SceneryPacksIniTest.java`, all FX.

**Interfaces:**
- Produces: `SceneryPackIniItem(String iniValue, boolean disabled)` with `of(String)`, `of(String, boolean)`, `getIniValue()`, `isDisabled()/setDisabled(boolean)`, `isToken()` (regex `^\*.+\*$`), `GLOBAL_AIRPORTS_MARKER`, `GLOBAL_AIRPORTS_FOLDER`.
- Produces: `static Path SceneryEntry.resolveFolder(SceneryPackIniItem item, Path baseFolder, Path globalAirportsFolder)` — path value → `baseFolder.resolve(iniValue)`; `*GLOBAL_AIRPORTS*` → `globalAirportsFolder`; other tokens → null.

- [ ] **Step 1: Update tests to the target API (RED)**

`SceneryEntryTest`: add 3 `resolveFolder` tests (path resolves against base; Global Airports token resolves; `*FOO*` → null); replace `new TokenSceneryPackIniItem(...)` with `SceneryPackIniItem.of(...)`. Parser tests: replace subclass ctors with `SceneryPackIniItem.of(v[, d])`. `SceneryManagerTest`: `e.getIniItem() != null && e.getIniItem().isToken()`. Rewrite `SceneryPackIniItemTest` to `of`/disabled-equality/`isToken` (no subclasses, no `resolveFolder`).

- [ ] **Step 2: Run tests, verify RED**

Run: `mvn test -pl xpman-api -Dtest='SceneryEntryTest,SceneryPackIniItemTest,SceneryIniFile*ParserTest,SceneryManagerTest'`
Expected: compilation FAILURE — `PathSceneryPackIniItem`/`TokenSceneryPackIniItem` still sealed-hierarchy classes; `SceneryEntry.resolveFolder(item, ...)` static doesn't exist; single-class equality vs subclass assertions.

- [ ] **Step 3: Implement the strip**

Flatten `SceneryPackIniItem` (fields + factories + `isToken` + constants, `@Getter @ToString @EqualsAndHashCode @AllArgsConstructor`, `@Setter` on `disabled`); delete the two subclasses; add the static `resolveFolder` to `SceneryEntry`; 1-line change in `SceneryManager.buildEntry`; constant swap in `GlobalAirportsSceneryPackage`; 2 Javadoc lines on `SceneryPackage.rank`/`enabled` ("derived stamp from the ini entry; authority lives on the entry/item").

- [ ] **Step 4: Run tests, verify GREEN**

Run: `mvn test -pl xpman-api`
Expected: PASS (manager tests gated by `@EnableOnLocalXPlane` if no local install).

- [ ] **Step 5: Full build (FX compiles untouched)**

Run: `mvn -B -DskipTests clean package`
Expected: BUILD SUCCESS across the 3 modules.

- [ ] **Step 6: Manual check + commit**

Grep `docs/manual/` for scenery internals mentions — expected none (zero user-visible change). Commit plan + code: `refactor(scenery): make SceneryPackIniItem a pure syntax type`
