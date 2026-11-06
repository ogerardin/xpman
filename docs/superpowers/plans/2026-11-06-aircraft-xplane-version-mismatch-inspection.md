# Aircraft/Sim Version Mismatch Inspection — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Block (ERROR) installation of an aircraft archive built for a newer X-Plane than the target install, and warn (WARN, non-blocking) when it was built for an older one.

**Architecture:** One new `Inspection<Archive>` in `install/inspections/` that reads each `.acf` entry inside the archive via `Archive.getAsText()` (no extraction), parses its spec version with the existing `AcfFileParser`, and compares it numerically against the target X-Plane's `specString()`. It is appended to the existing chain in `AircraftInstallableType.preconditions()`. No UI change: wizard page 2 already renders severity/message/details and disables **Next** on any ERROR.

**Tech Stack:** Java 25, JUnit 5 + Hamcrest + Mockito, Lombok, existing `ZipArchive` + `AcfFileParser`. No new dependencies.

**Spec:** design agreed in conversation 2026-11-06, condensed below (no separate spec file exists).

## Design (condensed spec)

- Scope: archives detected as aircraft by `AircraftInstallableType` only.
- ACF spec < sim spec (e.g. `1100` into XP12) → **WARN**, non-blocking.
- ACF spec > sim spec (e.g. `1200` into XP11) → **ERROR**, blocks Next. **No abort flag** — the chain continues; the `Assert*` prefix is reserved for aborting inspections, hence the `*Inspection` suffix.
- Sim version undetectable (`xPlane.getVersion() == null`), no `.acf` entries, or ACFs that fail to parse → skip silently (house philosophy: a missing probe never cries wolf).
- All `.acf` entries are checked (aircraft variants/packs); aggregate to a **single worst-case message**; the details column lists each checked ACF + its spec.
- Spec versions compare **numerically** — legacy 3-digit specs (e.g. `900`) must not compare lexicographically above `1100`.
- Non-goals: filename-based version sniffing, non-aircraft archives, XP10 special-casing, installed-aircraft checks (`AircraftSpecInspection` in the Diag panel already covers those).

## Global Constraints

- Java 25, JPMS module `xpman.api` — the new class lands in the already-exported `install.inspections` package; no `module-info` change.
- Zero-boilerplate house style: Lombok, streams; one-line class Javadoc, no `@author`.
- Messages use `message` + `details` only — wizard page 2 (`page2.fxml`) renders only Severity / Message / Details; `object` is not shown.
- **Never commit** (AGENTS.md): leave all work uncommitted in the working tree; this plan has no commit steps.
- The user manual chapter `docs/manual/09-installing-addons.md` must be updated in the same change (AGENTS.md).

## Review Focus

Failure modes the spec implies; each pinned by a test in its owning task:

1. Unparseable/garbage `.acf` content → skip that entry, never throw — Task 1 `unparseableAcfIsSkipped`.
2. Undetectable sim version → whole check skipped — Task 1 `undetectableSimVersionIsSkipped`.
3. Multi-ACF archive with mixed specs → single worst-case message, not one row per file — Task 1 `mixedSpecsYieldSingleWorstCaseMessage`.
4. Legacy 3-digit spec (`900`) → numeric comparison, not lexicographic — Task 1 `legacySpecComparesNumerically`.
5. Existing test fixtures with dummy ACF content keep passing (this inspection stays silent on them) — Task 1 skip test + Task 3 full-module test run.

---

### Task 1: `AircraftSpecVersionInspection`

**Files:**
- Create: `xpman-api/src/main/java/com/ogerardin/xplane/install/inspections/AircraftSpecVersionInspection.java`
- Test: `xpman-api/src/test/java/com/ogerardin/xplane/test/install/AircraftSpecVersionInspectionTest.java`

**Interfaces:**
- Consumes: `Inspection<Archive>#inspect`, `InspectionResult.of/empty`, `InspectionMessage.builder()` (`.severity/.message/.details/.build`), `Severity`, `Archive#getPaths/#getAsText(Path)`, `new AcfFileParser().parse(String)` → `getHeader().getSpecVersion()`, `XPlane#getVersion/#getMajorVersion`, `XPlaneMajorVersion#specString()` → `"1100"`/`"1200"`, `XPlaneMajorVersion#getMajor()`.
- Produces: `public class AircraftSpecVersionInspection implements Inspection<Archive>` with ctor `AircraftSpecVersionInspection(XPlane xPlane)` (Lombok `@RequiredArgsConstructor`).

- [ ] **Step 1: Write the failing test class**

Test package: `com.ogerardin.xplane.test.install` (mirrors `AssertXPlaneVersionAtLeastTest`). Pure unit test — no `@EnableOnLocalXPlane`.

```java
class AircraftSpecVersionInspectionTest {

    @TempDir
    Path tempFolder;

    @Test
    void newerAcfBlocksAsError() throws IOException {
        var result = inspect(acfZip("1200"), versionMock("11.55.1", XPlaneMajorVersion.XP11));
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSeverity(), is(Severity.ERROR));
        assertThat(result.get(0).getMessage(), both(containsString("X-Plane 12")).and(containsString("will not work")));
    }

    @Test
    void olderAcfWarns() throws IOException {
        var result = inspect(acfZip("1100"), versionMock("12.0.9", XPlaneMajorVersion.XP12));
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSeverity(), is(Severity.WARN));
        assertThat(result.get(0).getMessage(), containsString("X-Plane 11"));
    }

    @Test
    void matchingSpecIsSilent() throws IOException {
        var result = inspect(acfZip("1200"), versionMock("12.0.9", XPlaneMajorVersion.XP12));
        assertThat(result.size(), is(0));
    }

    @Test
    void undetectableSimVersionIsSkipped() throws IOException {
        var result = inspect(acfZip("1200"), versionMock(null, null));
        assertThat(result.size(), is(0));
    }

    @Test
    void unparseableAcfIsSkipped() throws IOException {
        Path zip = zipOf(Map.of("TestAircraft/TestAircraft.acf", "dummy acf content"));
        var result = inspect(zip, versionMock("12.0.9", XPlaneMajorVersion.XP12));
        assertThat(result.size(), is(0));
    }

    @Test
    void legacySpecComparesNumerically() throws IOException {
        // lexicographic compare would rank "900" above "1200" and wrongly produce an error
        var result = inspect(acfZip("900"), versionMock("12.0.9", XPlaneMajorVersion.XP12));
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSeverity(), is(Severity.WARN));
    }

    @Test
    void mixedSpecsYieldSingleWorstCaseMessage() throws IOException {
        Path zip = zipOf(Map.of(
                "TestAircraft/VariantA.acf", acfContent("1100"),
                "TestAircraft/VariantB.acf", acfContent("1200")));
        var result = inspect(zip, versionMock("11.55.1", XPlaneMajorVersion.XP11));
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSeverity(), is(Severity.ERROR));
        assertThat(result.get(0).getDetails(), both(containsString("VariantA.acf")).and(containsString("VariantB.acf")));
    }

    private InspectionResult inspect(Path zip, XPlane xPlane) throws IOException {
        return new AircraftSpecVersionInspection(xPlane).inspect(new ZipArchive(zip));
    }

    private Path acfZip(String spec) throws IOException {
        return zipOf(Map.of("TestAircraft/TestAircraft.acf", acfContent(spec)));
    }

    private Path zipOf(Map<String, String> entries) throws IOException {
        Path zip = Files.createTempFile(tempFolder, "aircraft", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            for (var entry : entries.entrySet()) {
                out.putNextEntry(new ZipEntry(entry.getKey()));
                out.write(entry.getValue().getBytes());
                out.closeEntry();
            }
        }
        return zip;
    }

    /** Minimal ACF accepted by AcfFileParser: origin / "NNNN Version" / "ACF" / blank / properties block. */
    private static String acfContent(String specVersion) {
        return """
                I
                %s Version
                ACF
                
                PROPERTIES_BEGIN
                P acf/_name Test Aircraft
                PROPERTIES_END
                """.formatted(specVersion);
    }

    private XPlane versionMock(String version, XPlaneMajorVersion major) {
        XPlane xPlane = mock(XPlane.class);
        when(xPlane.getVersion()).thenReturn(version);
        when(xPlane.getMajorVersion()).thenReturn(major);
        return xPlane;
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `mvn test -pl xpman-api -Dtest=AircraftSpecVersionInspectionTest`
Expected: compilation failure — `AircraftSpecVersionInspection` does not exist.

- [ ] **Step 3: Implement the class**

The comparison/aggregation algorithm and exact message copy are pinned here (they are the decisions):

```java
/**
 * Inspection that flags an aircraft archive whose ACF spec version does not match the target X-Plane version.
 */
@RequiredArgsConstructor
public class AircraftSpecVersionInspection implements Inspection<Archive> {

    private final XPlane xPlane;
    private final AcfFileParser parser = new AcfFileParser();

    private record AcfSpec(Path acf, int spec) {}

    @Override
    public InspectionResult inspect(Archive archive) {
        // house philosophy: missing probe skips the check
        if (xPlane.getVersion() == null) {
            return InspectionResult.empty();
        }
        int xPlaneSpec = Integer.parseInt(xPlane.getMajorVersion().specString());
        List<AcfSpec> acfSpecs = archive.getPaths().stream()
                .filter(path -> path.getFileName().toString().endsWith(".acf"))
                .map(path -> parseSpec(archive, path))
                .flatMap(Optional::stream)
                .toList();
        if (acfSpecs.isEmpty()) {
            return InspectionResult.empty();
        }
        String details = acfSpecs.stream()
                .map(s -> "%s: spec %d".formatted(s.acf().getFileName(), s.spec()))
                .collect(Collectors.joining("\n"));
        int maxSpec = acfSpecs.stream().mapToInt(AcfSpec::spec).max().orElseThrow();
        int minSpec = acfSpecs.stream().mapToInt(AcfSpec::spec).min().orElseThrow();
        if (maxSpec > xPlaneSpec) {
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.ERROR)
                    .message("Aircraft is intended for X-Plane %d (ACF spec %d) and will not work in X-Plane %d"
                            .formatted(maxSpec / 100, maxSpec, xPlane.getMajorVersion().getMajor()))
                    .details(details)
                    .build());
        }
        if (minSpec < xPlaneSpec) {
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.WARN)
                    .message("Aircraft is intended for X-Plane %d (ACF spec %d); it should still work in X-Plane %d, but a native version may be available"
                            .formatted(minSpec / 100, minSpec, xPlane.getMajorVersion().getMajor()))
                    .details(details)
                    .build());
        }
        return InspectionResult.empty();
    }

    private Optional<AcfSpec> parseSpec(Archive archive, Path acf) {
        try {
            int spec = Integer.parseInt(parser.parse(archive.getAsText(acf)).getHeader().getSpecVersion());
            return Optional.of(new AcfSpec(acf, spec));
        } catch (Exception e) {
            return Optional.empty(); // unreadable or unparseable ACF: skip
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -pl xpman-api -Dtest=AircraftSpecVersionInspectionTest`
Expected: PASS (7 tests). If `acfContent` fails to parse, re-check the text block: the line between `ACF` and `PROPERTIES_BEGIN` must be empty, and the spec line must read e.g. `1100 Version`.

### Task 2: Wire into `AircraftInstallableType.preconditions()`

**Files:**
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/install/types/AircraftInstallableType.java:48-54`
- Test: `xpman-api/src/test/java/com/ogerardin/xplane/test/install/AircraftInstallableTypeTest.java` (add one test + two helpers)

**Interfaces:**
- Consumes: Task 1's `AircraftSpecVersionInspection(XPlane)`; `Inspection.empty()`; the OpenSam-test `versionMock` idiom extended with `getAircraftManager()` (the chain's `WouldOverwriteInspection` needs it).
- Produces: nothing downstream.

- [ ] **Step 1: Write the failing wiring test**

Add to `AircraftInstallableTypeTest` (imports: `InspectionMessage`, `Severity`, `XPlane`, `XPlaneMajorVersion`, Mockito `mock`/`when`, `InspectionResult`):

```java
@Test
void preconditionsFlagVersionMismatch() throws Exception {
    Path xp12Aircraft = createVersionedAircraftArchive("1200");
    var intoXp11 = new AircraftInstallableType()
            .preconditions(versionMock("11.55.1", XPlaneMajorVersion.XP11), new ZipArchive(xp12Aircraft));
    assertThat(intoXp11.stream().anyMatch(InspectionMessage::isError), is(true));

    Path xp11Aircraft = createVersionedAircraftArchive("1100");
    var intoXp12 = new AircraftInstallableType()
            .preconditions(versionMock("12.0.9", XPlaneMajorVersion.XP12), new ZipArchive(xp11Aircraft));
    assertThat(intoXp12.stream().noneMatch(InspectionMessage::isError), is(true));
    assertThat(intoXp12.stream().anyMatch(m ->
            m.getSeverity() == Severity.WARN && m.getMessage().contains("intended for")), is(true));
}

/** Single-root aircraft archive with a parseable ACF of the given spec version. */
private Path createVersionedAircraftArchive(String spec) throws IOException {
    Path zip = Files.createTempFile(tempFolder, "aircraft", ".zip");
    try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
        out.putNextEntry(new ZipEntry("TestAircraft/"));
        out.closeEntry();
        out.putNextEntry(new ZipEntry("TestAircraft/TestAircraft.acf"));
        out.write(acfContent(spec).getBytes());
        out.closeEntry();
    }
    return zip;
}

private XPlane versionMock(String version, XPlaneMajorVersion major) throws Exception {
    XPlane real = new XPlane(tempFolder);
    XPlane xPlane = mock(XPlane.class);
    when(xPlane.getVersion()).thenReturn(version);
    when(xPlane.getMajorVersion()).thenReturn(major);
    when(xPlane.getPaths()).thenReturn(real.getPaths());
    when(xPlane.getAircraftManager()).thenReturn(real.getAircraftManager());
    return xPlane;
}
```

The `acfContent` fixture helper is identical to Task 1's — copy it in (house style duplicates small helpers per test class).

- [ ] **Step 2: Run to verify it fails**

Run: `mvn test -pl xpman-api -Dtest=AircraftInstallableTypeTest`
Expected: `preconditionsFlagVersionMismatch` FAILS (no spec-version message; `anyMatch(isError)` false / WARN absent). The pre-existing `recognizes*` tests still pass.

- [ ] **Step 3: Add the inspection to the chain**

In `AircraftInstallableType.preconditions()`, insert one line after the `WouldOverwriteInspection` line, matching its null-guard idiom:

```java
.and(xPlane == null ? Inspection.empty() : new AircraftSpecVersionInspection(xPlane))
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -pl xpman-api -Dtest=AircraftInstallableTypeTest`
Expected: PASS — including the pre-existing `recognizes*` tests (their dummy ACFs are unparseable → the new inspection stays silent).

### Task 3: Manual update + full verification

**Files:**
- Modify: `docs/manual/09-installing-addons.md` (Page 2 section)

- [ ] **Step 1: Document the new check**

After the paragraph ending "...Warnings are non-blocking — you can proceed past them." (line 32), add:

> For aircraft, XPman compares the archive's ACF spec version with your X-Plane version: an aircraft built for a **newer** X-Plane than yours is reported as an error (**Next** is disabled — it will not work), while an aircraft built for an **older** X-Plane only produces a warning you can proceed past (an updated, native build of that aircraft may be available).

- [ ] **Step 2: Run the full xpman-api test suite**

Run: `mvn test -pl xpman-api`
Expected: PASS — no existing test regressed.

- [ ] **Step 3: Verify the manual PDF still builds**

Run: `./scripts/build-manual.sh 0.0.0-DEV`
Expected: PDF builds without error.

- [ ] **Step 4: Report**

Report the change set, uncommitted, per AGENTS.md. No commit.
