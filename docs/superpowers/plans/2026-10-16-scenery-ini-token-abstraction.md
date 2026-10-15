# Scenery ini token abstraction (types + domain enum) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore the file format's type system — `SceneryPackIniItem` sealed into `Path`/`Token` subclasses (pure syntax, zero domain knowledge) — and make token *meaning* polymorphic via a domain enum with behavior, so a new token is one enum constant and a new item kind is a compiler error, not an if-chain edit.

**Architecture:** Shape is syntax (sealed hierarchy + `*…*` classification in `of()`); meaning is domain (`SceneryIniToken` enum, marker → `resolveFolder(XPlane)`). `SceneryEntry.resolveFolder(item, XPlane)` becomes an exhaustive sealed pattern switch. All strip-round wins preserved: `resolveFolder` stays in the domain, `GLOBAL_AIRPORTS_FOLDER` stays on `GlobalAirportsSceneryPackage`, string-valued items keep the trailing-separator/backslash round-trip fixes, getter-style accessors keep FX/SpEL untouched.

**Tech Stack:** Java 25 (sealed types + pattern matching), Lombok, JUnit 5 + Hamcrest.

**Spec:** Design agreed in session (2026-10-16), approved by the author ("go"). Predecessors: `docs/superpowers/plans/2026-10-16-scenery-ini-item-strip.md`, `docs/superpowers/specs/2026-08-30-scenery-ini-driven-design.md`.

## Global Constraints

- Zero user-visible behavior change → no manual update expected (verify by grep).
- Parser (`SceneryPacksIniParser`), data holder, ini writer, and all FX code stay untouched: `of(String[, boolean])`, `getIniValue()`, `isDisabled()`, `isToken()`, `getIniItem()` surface preserved.
- Items stay string-valued (not `Path`) and Lombok classes (not records): `disabled` must stay mutable for the manager's aliasing; getter-style accessors keep FX SpEL (`iniItem.disabled`) and the writer stable.
- `GLOBAL_AIRPORTS_MARKER` moves to the domain enum; the syntax layer keeps only the `*…*` shape knowledge.
- `resolveFolder` needs a real install (version-aware): ungated tests pin classification + enum dispatch only; folder resolution stays pinned by the gated `SceneryManagerTest` (already asserts token rows end-to-end).

## Review Focus

1. Classification: `of("*…*")` → `TokenSceneryPackIniItem`, else `PathSceneryPackIniItem` with trailing separators stripped — pinned ungated (`SceneryPackIniItemTest`).
2. Enum dispatch: known marker → `Optional.of(GLOBAL_AIRPORTS)`, unknown → empty — pinned ungated (`SceneryIniTokenTest`).
3. Exhaustive switch: dropping a sealed branch or adding an item kind must fail to compile (compiler-guaranteed).
4. Token→folder resolution on a real install — pinned gated (`SceneryManagerTest` token rows `IN_INI`).
5. Parser equality relies on per-subclass `(value, disabled)` equality — pinned by the 3 parser suites (unchanged).

---

### Task 1: Sealed types + domain token enum

**Files:**
- Rewrite: `xpman-api/src/main/java/com/ogerardin/xplane/file/data/scenery/SceneryPackIniItem.java` (abstract sealed base: `@Setter boolean disabled`, protected ctor, `of()` classifying by shape + stripping trailing separators on path values, abstract `getIniValue()`/`isToken()`)
- Create: `xpman-api/src/main/java/com/ogerardin/xplane/file/data/scenery/PathSceneryPackIniItem.java` (`String folder`, ctors `(String[, boolean])`, `isToken() → false`)
- Create: `xpman-api/src/main/java/com/ogerardin/xplane/file/data/scenery/TokenSceneryPackIniItem.java` (`String token`, ctors, `isToken() → true`)
- Create: `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryIniToken.java` (enum: `GLOBAL_AIRPORTS("*GLOBAL_AIRPORTS*")` with `resolveFolder(XPlane) → xPlane.getPaths().globalAirports()`; abstract `resolveFolder(XPlane)`; static `Optional<SceneryIniToken> of(String token)`)
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryEntry.java` (`resolveFolder(SceneryPackIniItem, XPlane)` — exhaustive sealed pattern switch; import `XPlane`, subclasses)
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryManager.java` (`SceneryEntry.resolveFolder(item, xPlane)`)
- Test: rewrite `xpman-api/src/test/java/com/ogerardin/xplane/test/file/SceneryPackIniItemTest.java` (classification, disabled equality, `isToken`, trailing-sep strip)
- Test: create `xpman-api/src/test/java/com/ogerardin/xplane/test/scenery/SceneryIniTokenTest.java` (marker dispatch, unknown → empty)
- Test: edit `xpman-api/src/test/java/com/ogerardin/xplane/test/scenery/SceneryEntryTest.java` (drop the 3 old-signature `resolveFolder` tests + now-unused hamcrest imports)
- Untouched: parser tests, `SceneryManagerTest`, `SceneryPacksIniTest`, parser/data/writer, all FX.

**Interfaces:**
- Produces: `SceneryPackIniItem.of(String[, boolean])` → `PathSceneryPackIniItem(folder)` | `TokenSceneryPackIniItem(token)`; abstract `getIniValue()`, `isToken()`.
- Produces: `static Path SceneryEntry.resolveFolder(SceneryPackIniItem item, XPlane xPlane)` — path → `xPlane.getBaseFolder().resolve(iniValue)`; token → enum lookup → `resolveFolder(xPlane)` or null.

- [ ] **Step 1: Update/add tests to the target API (RED)** — rewrite `SceneryPackIniItemTest` (subclass ctors + classification), add `SceneryIniTokenTest`, drop the 3 old-signature `resolveFolder` tests from `SceneryEntryTest`.
- [ ] **Step 2: Run tests, verify RED** — `mvn test -pl xpman-api -Dtest='SceneryPackIniItemTest,SceneryIniTokenTest,SceneryEntryTest'` → compilation failure (subclasses/enum/switch don't exist yet).
- [ ] **Step 3: Implement** — sealed base + two subclasses + enum + `SceneryEntry` switch + manager call.
- [ ] **Step 4: Run tests, verify GREEN** — `mvn test -pl xpman-api` → PASS (manager tests gated on local X-Plane).
- [ ] **Step 5: Full build** — `mvn -B -DskipTests clean package` → BUILD SUCCESS (FX untouched).
- [ ] **Step 6: Manual check + commit** — grep `docs/manual/` (expect no mentions); commit plan + code: `refactor(scenery): restore ini item types, add SceneryIniToken domain enum`.
