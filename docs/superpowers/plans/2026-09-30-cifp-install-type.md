# FAA CIFP (ARINC 424) Installable Type Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let XPman install free FAA CIFP navigation data by extracting `FAACIFP18` from a user-supplied FAA cycle ZIP, running the `convert424toxplane` converter (downloaded and cached on first use), and writing `earth_424.dat` plus the generated `CIFP/` folder into `Custom Data`.

**Architecture:** A new `CifpInstallableType` is a thin `InstallableType` adapter (mirroring `NavDataInstallableType`) that recognizes archives containing an entry named exactly `FAACIFP18` and delegates all filesystem/process work to a new `CifpManager`. The wizard resolves the type automatically via `IntrospectionHelper`, so there are no `xpman-fx` changes. Converter download failures and a non-runnable binary abort with `InstallationException`; a non-zero converter exit is non-fatal (warn, skip `CIFP/`, still install `earth_424.dat`).

**Tech Stack:** Java 25, JPMS, Lombok (`@Slf4j`, `@UtilityClass`, `@Getter(lazy=true)`), JUnit Jupiter 5 + Hamcrest, Apache Commons IO, `java.util.zip` via the existing `ZipArchive`, `CommandExecutor` for the external process.

---

## Deviations from the design doc (read this first)

Two implementation-time findings required changing the spec's approach. The design's *intent* is unchanged; only the mechanism differs.

### 1. `ToolUtils.installFromZip` cannot be used for the converter

The design doc (step 3 of Converter resolution) said to pass a resolved `<os>` root as the filter to `ToolUtils.installFromZip`. Reading `ToolUtils.java:119-148` shows it filters entries with `entryPath.equals(file) || entryPath.startsWith(file)`. Since `Path.startsWith` is true only when the argument is the *leading* path elements, and the Dropbox ZIP nests everything under an **unknown top-level directory**, the unknown prefix must be known *before* filtering. `installFromZip` downloads internally and then filters, so it cannot be given that prefix.

**Resolution used in this plan:** `CifpManager` downloads the ZIP to its own temp file, wraps it in `ZipArchive`, scans `getPaths()` to locate the `<os>` segment, and extracts with the **subpath overload** `extract(toolsFolder, osRoot, progress)`. That overload strips `osRoot` (`ZipArchive.java:134`), which is exactly what discards the unknown prefix — the predicate overload would have preserved it. `ToolUtils` is therefore not used.

### 3. Platform dispatch is polymorphic, not conditional

Mapping a platform to its converter folder and binary name is the kind of platform-specific
behavior `Platform` already models (see `pluginPathIdentifier()` at `Platform.java:124-126`,
and the AGENTS.md rule "Always prefer adding a method to the `Platform` interface over
`if/else` on platform type"). This plan adds two hooks to `Platform`:

- `default String cifpConverterFolder()` — the archive subfolder, `null` by default
- `default String cifpConverterName(XPlaneMajorVersion)` — the executable, `null` by default

Both are overridden in `MacPlatform`, `WindowsPlatform`, and `LinuxPlatform`. The XP11/XP12
binary choice lives in the `WindowsPlatform` override, because Windows is the only platform
that ships two executables — so even the version branch is not an `if/else` in `CifpManager`.
`CifpManager` contains no `if/else` on `getOsType()` and no `WINDOWS.equals(folder)` test.

### 4. `install.types` must be exported to the test module

`com.ogerardin.xplane.install.types` is **not** exported by `module-info.java`, so a test in
`xpman.api.test` cannot import `CifpInstallableType` — javac fails with *"package
com.ogerardin.xplane.install.types is not visible"*. This is why no existing test references
an install type directly. The project already has the convention for exactly this
(`module-info.java:35,51,52`), so Task 1 adds:

```java
    exports com.ogerardin.xplane.install.types to xpman.api.test;
```

The test-side package `com.ogerardin.xplane.test.install` is already opened to
`org.junit.platform.commons` (`src/test/java/module-info.java:29`), so no change is needed
there.

### 5. Spec wording corrections

- Design doc line 38 said "One class … No changes to … existing install types" while its own Files table creates **two** classes and modifies `XPlane.java`. The Files table is authoritative; this plan follows it. The design doc is updated in Task 6.
- Design doc line 34 claimed the cache "survives X-Plane directory re-creation". It does not — `Resources/tools` lives inside the X-Plane folder. The design doc is corrected in Task 6.

---

## File Structure

| Action | File | Responsibility |
|---|---|---|
| **Create** | `xpman-api/src/main/java/com/ogerardin/xplane/install/types/CifpInstallableType.java` | `InstallableType` adapter: `recognizes()` on the `FAACIFP18` entry name, empty `preconditions()`, delegates `install()` to `CifpManager` |
| **Create** | `xpman-api/src/main/java/com/ogerardin/xplane/navdata/CifpManager.java` | Owns converter resolution/download/cache, process execution, and the `Custom Data` copy |
| **Modify** | `xpman-api/src/main/java/com/ogerardin/xplane/XPlane.java:47-53` | Add `@Getter(lazy=true) private final CifpManager cifpManager = new CifpManager(this);` beside the other managers |
| **Modify** | `xpman-api/src/main/java/com/ogerardin/xplane/util/platform/Platform.java` | Add `default String cifpConverterFolder()` and `default String cifpConverterName(XPlaneMajorVersion)`, both returning `null`, mirroring the existing `pluginPathIdentifier()` hook |
| **Modify** | `xpman-api/.../util/platform/MacPlatform.java` | Override both: `"mac"` + extensionless `convert424toxplane` |
| **Modify** | `xpman-api/.../util/platform/WindowsPlatform.java` | Override both: `"windows"` + `convert424toxplane11.exe` (XP11) / `convert424toxplane.exe` (XP12) |
| **Modify** | `xpman-api/.../util/platform/LinuxPlatform.java` | Override both: `"linux"` + extensionless `convert424toxplane` |
| **Modify** | `xpman-api/src/main/java/module-info.java:44` | `exports com.ogerardin.xplane.install.types to xpman.api.test;` so the test can import the type |
| **Create** | `xpman-api/src/test/java/com/ogerardin/xplane/test/install/CifpInstallableTypeTest.java` | Recognition test over temp-file ZIPs |
| **Modify** | `docs/superpowers/specs/2026-09-30-cifp-install-type-design.md` | Fix the two wording inconsistencies listed above |

**Responsibility boundary:** `CifpInstallableType` decides *whether* an archive is CIFP and translates exceptions. `CifpManager` does *all* I/O. Neither touches the UI.

### Established APIs this plan relies on (verified against the source)

- `InstallableType`: `String description()`, `boolean recognizes(Archive)`, `InspectionResult preconditions(XPlane, Archive)`, `void install(XPlane, Archive, ProgressListener) throws InstallationException` — `NavDataInstallableType.java:21-46`.
- `Archive`: `List<Path> getPaths()`, `void extract(Path folder, Predicate<Path> filter, ProgressListener)`, `String getAsText(Path)` — `Archive.java:17-44`.
- `InspectionResult.empty()` — `InspectionResult.java:21`.
- `CommandExecutor.builder().cmdarray(String[]).dir(Path).outLineHandler(Consumer<String>).errLineHandler(Consumer<String>).build().exec()` returns `ExecResults` — `CommandExecutor.java:26-75`.
- `ExecResults`: `isSuccessful()`, `getExitValue()`, `outputLines()`, `errorLines()` — `ExecResults.java:14-24`.
- `Platforms.getCurrent()` (Lombok `@Getter(lazy=true)` on the static `current` field) returns the current `Platform`; `Platform` exposes `getOsType()`, `getCpuType()`, `isRunnable(Path)`, `isQuarantined(Path)`, `removeQuarantine(Path)` — `Platforms.java:23-24`, `Platform.java:21-101`. Note the static field is *typed* `Platform`, so `getCurrent()` returns `Platform`, not the `Platforms` enum.
- `XPlane.getPaths().tools()` → `Resources/tools`; `getPaths().customData()` → `Custom Data`; `getMajorVersion()`; `getNavDataManager().reload()` — `XPlane.java:78-115`, `XPlane.java:33,48,100-111`.
- `ZipArchive(Path zipFile)` — `ZipArchive.java:27`. It **overrides** the subpath overload `extract(Path, Path subpath, ProgressListener)` (`ZipArchive.java:96-101`), which extracts everything under `subpath` with the prefix stripped (`ZipArchive.java:134`). The predicate overload does *not* strip anything — use the subpath form when the unknown Dropbox prefix must be discarded.
- `XPlaneMajorVersion.XP11` / `XP12` — `XPlaneMajorVersion.java`.
- `NavDataManager` already expects `earth_424.dat` in `Custom Data` (`NavDataManager.java:66,88`), so the target filename is fixed by existing code, not a new choice.

---

## Task 1: Recognition test (failing first)

This is the only pure, platform-independent logic, so it is the only thing with a real automated test. Everything else is verified manually in Task 5.

**Files:**
- Modify: `xpman-api/src/main/java/module-info.java:44` — export `install.types` to the test module
- Create: `xpman-api/src/test/java/com/ogerardin/xplane/test/install/CifpInstallableTypeTest.java`
- Test: same file

- [ ] **Step 1: Export the install types package to the test module**

Without this, the test below cannot compile: `com.ogerardin.xplane.install.types` is not
exported, so `xpman.api.test` gets *"package ... is not visible"*. Follow the existing
convention used for `aircraft.custom` and `file.data` by adding, next to the other
`exports` lines in `xpman-api/src/main/java/module-info.java`:

```java
    exports com.ogerardin.xplane.install.types to xpman.api.test;
```

- [ ] **Step 2: Write the failing test**

Create the test file. `ZipArchive` takes a `Path`, so each case writes a real temp ZIP with `ZipOutputStream` — there is no in-memory archive implementation. The temp file is cleaned up by JUnit's `@TempDir`.

```java
package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.install.ArchiveInstallSource;
import com.ogerardin.xplane.install.types.CifpInstallableType;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests recognition of FAA CIFP cycle archives by CifpInstallableType.
 */
class CifpInstallableTypeTest {

    @TempDir
    Path tempFolder;

    @Test
    void recognizesArchiveContainingFaacifp18() throws IOException {
        Path zip = zipNamed("FAACIFP18");

        assertThat(new CifpInstallableType().recognizes(new ZipArchive(zip)), is(true));
    }

    @Test
    void recognizesArchiveContainingNestedFaacifp18() throws IOException {
        Path zip = zipNamed(Path.of("CIFP", "2026-09-30", "FAACIFP18"));

        assertThat(new CifpInstallableType().recognizes(new ZipArchive(zip)), is(true));
    }

    @Test
    void doesNotRecognizeNavDataArchive() throws IOException {
        Path zip = zipNamed("earth_424.dat");

        assertThat(new CifpInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void doesNotRecognizeArchiveContainingDifferentFile() throws IOException {
        Path zip = zipNamed("FAACIFP18.dat");

        assertThat(new CifpInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void isDiscoveredAsAnInstallableType() throws IOException {
        Path zip = zipNamed("FAACIFP18");

        ArchiveInstallSource source = ArchiveInstallSource.ofZip(zip);

        assertThat(source.getInstallableType().isPresent(), is(true));
    }

    /**
     * Creates a single-entry zip whose entry has the given name and the given content.
     */
    private Path zipNamed(String entryName) throws IOException {
        Path zip = Files.createTempFile(tempFolder, "test", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry(entryName));
            out.write("test content".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    private Path zipNamed(Path entryName) throws IOException {
        return zipNamed(entryName.toString());
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `mvn test -pl xpman-api -Dtest=CifpInstallableTypeTest`

Expected: compilation failure — `cannot find symbol: class CifpInstallableType`. This is the correct "red" state: the class under test does not exist yet.

- [ ] **Step 4: Do not proceed until Step 3 confirms the failure**

If the build reports a *different* error (for example an unrelated test compilation failure), fix that first so the red state is meaningful.

- [ ] **Step 5: Commit the failing test**

```bash
git add xpman-api/src/main/java/module-info.java \
        xpman-api/src/test/java/com/ogerardin/xplane/test/install/CifpInstallableTypeTest.java
git commit -m "test: add failing recognition test for FAA CIFP install type"
```

---

## Task 2: `CifpManager` — converter resolution and download

`CifpManager` is created **before** `CifpInstallableType` so that no commit in this plan
leaves the module non-compiling.

**Files:**
- Create: `xpman-api/src/main/java/com/ogerardin/xplane/navdata/CifpManager.java`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/XPlane.java:47-53`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/util/platform/Platform.java`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/util/platform/MacPlatform.java`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/util/platform/WindowsPlatform.java`
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/util/platform/LinuxPlatform.java`

- [ ] **Step 1: Wire the manager into `XPlane`**

In `XPlane.java`, add the import and the field. The existing manager block is:

```java
    @Getter(lazy = true)
    @ToString.Exclude
    private final NavDataManager navDataManager = new NavDataManager(this);

    @Getter(lazy = true)
    @ToString.Exclude
    private final ToolsManager toolsManager = new ToolsManager(this);
```

Insert `cifpManager` immediately after `navDataManager` (the spec calls for "beside `navDataManager` (line 49)"), preserving the surrounding annotation pattern:

```java
    @Getter(lazy = true)
    @ToString.Exclude
    private final NavDataManager navDataManager = new NavDataManager(this);

    @Getter(lazy = true)
    @ToString.Exclude
    private final CifpManager cifpManager = new CifpManager(this);

    @Getter(lazy = true)
    @ToString.Exclude
    private final ToolsManager toolsManager = new ToolsManager(this);
```

And add the import next to the other `com.ogerardin.xplane.*` imports:

```java
import com.ogerardin.xplane.navdata.CifpManager;
```

- [ ] **Step 2: Add the platform hook**

Following the existing `pluginPathIdentifier()` pattern in `Platform.java:124-126`, add two
default methods: one naming the platform's folder in the converter archive, one naming its
executable. This keeps *all* platform dispatch polymorphic — neither the folder choice nor
the binary-name choice appears as an `if/else` or a folder-string comparison in `CifpManager`.

In `xpman-api/src/main/java/com/ogerardin/xplane/util/platform/Platform.java`, add
alongside the other `default` methods (for example, immediately before
`getCandidateInstallBaseFolders`), plus the import
`import com.ogerardin.xplane.XPlaneMajorVersion;`:

```java
    /**
     * The name of the folder holding the convert424toxplane binary inside the converter
     * distribution archive, or null if this platform has no CIFP converter.
     */
    default String cifpConverterFolder() {
        return null;
    }

    /**
     * The name of the convert424toxplane executable for the given X-Plane version,
     * or null if this platform has no CIFP converter.
     */
    default String cifpConverterName(XPlaneMajorVersion majorVersion) {
        return null;
    }
```

Then override **both** methods in each platform implementation. Each of `MacPlatform`,
`WindowsPlatform`, and `LinuxPlatform` also needs its own
`import com.ogerardin.xplane.XPlaneMajorVersion;` (only `Platform.java` gets it from the
block above). macOS and Linux ship one extensionless binary that serves every X-Plane
version:

`MacPlatform.java`:

```java
    @Override
    public String cifpConverterFolder() {
        return "mac";
    }

    @Override
    public String cifpConverterName(XPlaneMajorVersion majorVersion) {
        return "convert424toxplane";
    }
```

`LinuxPlatform.java`:

```java
    @Override
    public String cifpConverterFolder() {
        return "linux";
    }

    @Override
    public String cifpConverterName(XPlaneMajorVersion majorVersion) {
        return "convert424toxplane";
    }
```

Windows is the only platform with two binaries — XP11 needs the versioned `.exe`, XP12 the
unversioned one — so that is the only place a version check belongs:

`WindowsPlatform.java`:

```java
    @Override
    public String cifpConverterFolder() {
        return "windows";
    }

    @Override
    public String cifpConverterName(XPlaneMajorVersion majorVersion) {
        return majorVersion == XPlaneMajorVersion.XP11
            ? "convert424toxplane11.exe"
            : "convert424toxplane.exe";
    }
```

`UnknownPlatform` keeps both defaults and therefore reports no converter, which
`CifpManager` turns into an aborting `InstallationException`.

- [ ] **Step 3: Write `CifpManager`**

```java
package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.util.platform.Platform;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import com.ogerardin.xplane.util.zip.ZipArchive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Installs FAA CIFP (ARINC 424) navigation data into an X-Plane installation.
 * Downloads and caches the third-party convert424toxplane converter, runs it over the
 * FAACIFP18 file found in the user's FAA cycle archive, and copies the results into
 * the Custom Data folder.
 */
@RequiredArgsConstructor
@Slf4j
public class CifpManager {

    /** The extensionless file the FAA ships the current cycle in. */
    private static final String FAACIFP18 = "FAACIFP18";

    /** Fixed HTTPS source for the converter, as a Dropbox folder zip download. */
    private static final URI CONVERTER_URL = URI.create(
        "https://www.dropbox.com/scl/fo/mnw9cufqcxgmkzpx35269/AG84gKEZWlR1Sk5Vld0csGk?rlkey=udqtjnhsdo0c7cnhbe2o0ft6o&dl=1"
    );

    private final XPlane xPlane;

    /**
     * Returns the platform folder name inside the converter archive, or null if this
     * platform has no CIFP converter.
     */
    static String osFolderName(Platform platform) {
        return platform.cifpConverterFolder();
    }

    /**
     * Returns the converter binary name for the given platform and X-Plane version.
     */
    static String converterName(Platform platform, XPlaneMajorVersion majorVersion) {
        return platform.cifpConverterName(majorVersion);
    }

    /**
     * Resolves the converter binary, downloading and caching it if it is not already
     * present and runnable under {@code XPlane/Resources/tools/<os>/}.
     *
     * @throws IOException if the converter cannot be downloaded or extracted
     */
    Path resolveConverter(ProgressListener progress) throws IOException {
        Platform platform = Platforms.getCurrent();
        String os = osFolderName(platform);
        if (os == null) {
            throw new IOException("No FAA CIFP converter is available for " + platform);
        }
        Path toolsFolder = xPlane.getPaths().tools().resolve(os);
        Path binary = toolsFolder.resolve(converterName(platform, xPlane.getMajorVersion()));

        if (Files.isExecutable(binary) && platform.isRunnable(binary)) {
            log.debug("Using cached CIFP converter {}", binary);
            return binary;
        }

        progress.output("Downloading CIFP converter");
        Files.createDirectories(toolsFolder);

        Path tempZip = Files.createTempFile("xpman-cifp-converter", ".zip");
        try {
            try (InputStream in = CONVERTER_URL.toURL().openStream()) {
                FileUtils.copyInputStreamToFile(in, tempZip.toFile());
            }
            progress.output("Converter archive contains " + new ZipArchive(tempZip).entryCount() + " entries");

            // The archive nests everything under an unknown top-level folder, so the
            // <os> subtree is located by scanning entry names rather than assumed.
            ZipArchive archive = new ZipArchive(tempZip);
            Path osRoot = findOsRoot(archive, os).orElseThrow(() -> new IOException(
                "CIFP converter archive contains no '" + os + "' folder"));
            log.debug("CIFP converter '{}' folder found at {} in the archive", os, osRoot);

            // Extract the whole <os> subtree so the Windows geoids folder, which must
            // sit next to the executable, is preserved. The subpath overload strips the
            // located root, discarding the unknown Dropbox prefix.
            archive.extract(toolsFolder, osRoot, progress);
        } finally {
            Files.deleteIfExists(tempZip);
        }

        if (!Files.exists(binary)) {
            throw new IOException("CIFP converter " + binary + " was not found in the archive");
        }
        if (platform.isQuarantined(binary)) {
            progress.output("Removing quarantine from " + binary.getFileName());
            platform.removeQuarantine(binary);
        }
        if (!platform.isRunnable(binary)) {
            throw new IOException("CIFP converter " + binary + " for " + os
                + " / X-Plane " + xPlane.getMajorVersion() + " is not runnable on this system");
        }
        return binary;
    }

    /**
     * Finds the archive-relative root folder of the {@code os} subtree, i.e. the entry
     * that is the parent of the first {@code <os>/convert424toxplane*} path.
     */
    private static Optional<Path> findOsRoot(Archive archive, String os) {
        return archive.getPaths().stream()
                .filter(path -> hasOsSegmentThenConverter(path, os))
                .map(path -> path.getParent())
                .min(Comparator.comparingInt(Path::getNameCount));
    }

    /** Shared stem of every convert424toxplane executable, with or without a suffix. */
    private static final String CONVERTER_STEM = "convert424toxplane";

    private static boolean hasOsSegmentThenConverter(Path path, String os) {
        return IntStream.range(0, path.getNameCount() - 1)
                .anyMatch(i -> path.getName(i).toString().equals(os)
                        && path.getName(i + 1).toString().toLowerCase(Locale.ROOT).startsWith(CONVERTER_STEM));
    }

    }
```

Notes on the design decisions in this class:

- `resolveConverter` throws `IOException` for every abort condition (download failed, `<os>` folder missing, binary missing, not runnable). `CifpInstallableType.install` wraps these in `InstallationException`, which is the spec's abort channel. A single exception type keeps the manager free of install-layer concerns.
- The download uses `FileUtils.copyInputStreamToFile`, the same Commons IO primitive `ToolUtils` itself uses (`ToolUtils.java:65,126`), rather than `copyURLToFile`, because the URL is built from a `URI` with a query string and the `InputStream` form avoids re-parsing.
- `Files.isExecutable` gates the cache hit so a partially-downloaded or permission-stripped binary is re-fetched instead of being reported as "not runnable".
- `findOsRoot` picks the *shallowest* matching root. If the archive ever ships both `x/convert424toxplane` and `x/backup/convert424toxplane`, the shallowest is the real root.

- [ ] **Step 4: Build the module**

Note that `-DskipTests` still *compiles* the tests, and `CifpInstallableTypeTest` references
`CifpInstallableType`, which does not exist until Task 3. Use `-Dmaven.test.skip=true` to skip
test compilation as well:

Run: `mvn -q -Dmaven.test.skip=true package -pl xpman-api`

Expected: BUILD SUCCESS. `CifpManager` compiles against the new `Platform` hooks, and
`XPlane` compiles with the new field.

- [ ] **Step 5: Confirm the rest of the suite is unaffected**

Run: `mvn -B test -pl xpman-api -Dtest='!CifpInstallableTypeTest'`

Expected: every other test class passes. Deferring `CifpInstallableTypeTest` itself to Task 3
is deliberate: it cannot compile until `CifpInstallableType` exists.

- [ ] **Step 6: Commit**

```bash
git add xpman-api/src/main/java/com/ogerardin/xplane/navdata/CifpManager.java \
        xpman-api/src/main/java/com/ogerardin/xplane/XPlane.java \
        xpman-api/src/main/java/com/ogerardin/xplane/util/platform/Platform.java \
        xpman-api/src/main/java/com/ogerardin/xplane/util/platform/MacPlatform.java \
        xpman-api/src/main/java/com/ogerardin/xplane/util/platform/WindowsPlatform.java \
        xpman-api/src/main/java/com/ogerardin/xplane/util/platform/LinuxPlatform.java
git commit -m "feat: add CifpManager resolving the convert424toxplane converter"
```

---

## Task 3: `CifpInstallableType` — recognition and adapter

`CifpManager` now exists (Task 2), so the adapter compiles and the Task 1 tests can go green.

**Files:**
- Create: `xpman-api/src/main/java/com/ogerardin/xplane/install/types/CifpInstallableType.java`
- Test: `xpman-api/src/test/java/com/ogerardin/xplane/test/install/CifpInstallableTypeTest.java`

- [ ] **Step 1: Write the class**

```java
package com.ogerardin.xplane.install.types;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.InstallableType;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.navdata.CifpManager;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;

import java.io.IOException;
import java.util.Objects;

/**
 * Installable type for FAA CIFP (ARINC 424) navigation data.
 * Recognizes archives containing an entry named exactly FAACIFP18 and installs the
 * converted X-Plane data set plus earth_424.dat into the Custom Data folder.
 */
@SuppressWarnings("unused")
public class CifpInstallableType implements InstallableType {

    /** The name of the extensionless file the FAA ships the current cycle in. */
    private static final String FAACIFP18 = "FAACIFP18";

    @Override
    public String description() {
        return "FAA CIFP (ARINC 424)";
    }

    @Override
    public boolean recognizes(Archive archive) {
        return archive.getPaths().stream()
                .anyMatch(path -> Objects.equals(path.getFileName().toString(), FAACIFP18));
    }

    @Override
    public InspectionResult preconditions(XPlane xPlane, Archive archive) {
        // Nothing to validate up front: the converter is fetched on demand and the
        // X-Plane destination paths are only written during install.
        return InspectionResult.empty();
    }

    @Override
    public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
        try {
            xPlane.getCifpManager().install(archive, progress);
        } catch (IOException e) {
            throw new InstallationException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InstallationException("CIFP conversion was interrupted", e);
        }
    }
}
```

Note on `recognizes()`: it compares `path.getFileName().toString()` to the constant rather than using `Path.endsWith(String)`. `Path.endsWith` compares whole *path elements*, so it happens to work for a top-level `FAACIFP18`, but the explicit filename comparison also matches nested occurrences (e.g. `CIFP/2026-09-30/FAACIFP18`) and is exact — `FAACIFP18.dat` is correctly rejected. The `recognizesArchiveContainingNestedFaacifp18` and `doesNotRecognizeArchiveContainingDifferentFile` tests pin this behavior.

- [ ] **Step 2: Run the recognition tests — all five should pass now**

Run: `mvn test -pl xpman-api -Dtest=CifpInstallableTypeTest`

Expected: all five tests pass, including `isDiscoveredAsAnInstallableType` (ClassGraph instantiates the class through its implicit no-arg constructor and `recognizes()` returns true).

- [ ] **Step 3: Verify the negative case really is negative**

Run: `mvn test -pl xpman-api -Dtest=CifpInstallableTypeTest#doesNotRecognizeArchiveContainingDifferentFile`

Expected: PASS. This guards the design's "no overlap with `NavDataInstallableType`" requirement, which depends on exact filename matching rather than a `startsWith`/`contains` check.

- [ ] **Step 4: Commit**

```bash
git add xpman-api/src/main/java/com/ogerardin/xplane/install/types/CifpInstallableType.java
git commit -m "feat: add CifpInstallableType recognizing FAA CIFP archives"
```

---

## Task 4: `CifpManager` — conversion and install flow

**Files:**
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/navdata/CifpManager.java`

- [ ] **Step 1: Add the install entry point**

Add these imports to `CifpManager.java` (`FileUtils` is already imported by Task 2, so
only these are genuinely new):

```java
import com.ogerardin.xplane.util.exec.CommandExecutor;
import com.ogerardin.xplane.util.exec.ExecResults;
```

`extractSource` now uses the subpath overload, so no extra imports are needed for it.

Add this method to the class:

```java
    /**
     * Installs the FAA CIFP data found in the specified archive.
     *
     * @throws IOException          if the archive cannot be read or the files cannot be written
     * @throws InterruptedException if the converter process is interrupted
     */
    public void install(Archive archive, ProgressListener progress)
            throws IOException, InterruptedException {

        Path workingDir = Files.createTempDirectory("xpman-cifp");
        try {
            Path source = extractSource(archive, workingDir, progress);
            Path converter = resolveConverter(progress);

            progress.output("Running " + converter.getFileName());
            ExecResults results = CommandExecutor.builder()
                    .cmdarray(new String[] { converter.toString(), source.getFileName().toString(), "FMS" })
                    .dir(workingDir)
                    .outLineHandler(progress::output)
                    .errLineHandler(progress::output)
                    .build()
                    .exec();

            if (results.isSuccessful()) {
                copyConvertedData(workingDir, progress);
            } else {
                String details = "CIFP conversion failed (exit " + results.getExitValue() + ")";
                log.warn("{}: {}", details, String.join("\n", results.errorLines()));
                progress.output(details + "; earth_424.dat will still be installed but no CIFP folder.");
            }

            copyToCustomData(source, progress);
            xPlane.getNavDataManager().reload();
        } finally {
            FileUtils.deleteQuietly(workingDir.toFile());
        }
    }

    /**
     * Extracts the FAACIFP18 entry from the archive into the working directory and
     * renames it to the .dat name the converter expects.
     *
     * The entry's parent folder, if any, is used as the extraction subpath so a nested
     * FAACIFP18 still lands at the root of the working directory under its own name.
     */
    private Path extractSource(Archive archive, Path workingDir, ProgressListener progress) throws IOException {
        Path entry = archive.getPaths().stream()
                .filter(path -> FAACIFP18.equals(path.getFileName().toString()))
                .findFirst()
                .orElseThrow(() -> new IOException("FAACIFP18 not found in the archive"));

        Path parent = entry.getParent();
        if (parent == null) {
            archive.extract(workingDir, progress);
        } else {
            archive.extract(workingDir, parent, progress);
        }

        Path extracted = workingDir.resolve(FAACIFP18);
        if (!Files.exists(extracted)) {
            throw new IOException("FAACIFP18 could not be extracted from the archive");
        }
        Path source = workingDir.resolve("FAACIFP18.dat");
        Files.move(extracted, source, StandardCopyOption.REPLACE_EXISTING);
        return source;
    }

    /**
     * Copies the CIFP folder the converter generated into Custom Data, overwriting
     * any existing entries.
     */
    private void copyConvertedData(Path workingDir, ProgressListener progress) throws IOException {
        Path generated = workingDir.resolve("CIFP");
        if (!Files.isDirectory(generated)) {
            log.warn("CIFP converter produced no CIFP folder in {}", workingDir);
            progress.output("CIFP converter produced no CIFP folder; only earth_424.dat was installed.");
            return;
        }
        Path target = xPlane.getPaths().customData().resolve("CIFP");
        progress.output("Copying CIFP data to " + target);
        FileUtils.copyDirectory(generated.toFile(), target.toFile());
    }

    /**
     * Copies the source file to Custom Data as earth_424.dat.
     */
    private void copyToCustomData(Path source, ProgressListener progress) throws IOException {
        Path target = xPlane.getPaths().customData().resolve("earth_424.dat");
        progress.output("Installing " + target.getFileName());
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
```

`FileUtils.copyDirectory` already merges into an existing target and overwrites files, so
it replaces the whole recursive walk *and* removes the need for the exception-wrapping
helper class. It is the same call `ToolUtils` uses for the DMG case
(`ToolUtils.java:87`).

- [ ] **Step 2: Clean up the working directory**

`FileUtils.deleteQuietly` handles the recursive delete and swallows failures, which is
exactly the required behaviour for a temporary folder, so the entire `finally` body and the
`UncheckedCopyException` class are unnecessary. In `install(...)`, the `finally` block
becomes:

```java
        } finally {
            FileUtils.deleteQuietly(workingDir.toFile());
        }
```

Then delete the `UncheckedCopyException` nested class from the class entirely — it exists
only to smuggle an `IOException` out of the old copy lambda, and nothing references it now.
Also drop the now-unused `java.util.stream.Stream` and `java.nio.file.StandardCopyOption`
imports if `StandardCopyOption` is no longer referenced (it still is, by `extractSource` and
`copyToCustomData`, so keep it; `Stream` is no longer referenced, so remove it).

`CifpManager` already imports `java.util.Comparator` (used by `findOsRoot`),
`java.util.Optional` (used by `findOsRoot`), and `java.util.stream.IntStream` (used by
`hasOsSegmentThenConverter`). Remove the `java.util.stream.Stream` import if Task 2 left it
unused — after this task nothing references it. `javac` will not complain about an unused
import, but the project style prefers none.

- [ ] **Step 3: Build the module**

Run: `mvn -q -DskipTests package -pl xpman-api`

Expected: BUILD SUCCESS.

- [ ] **Step 4: Run the tests**

Run: `mvn test -pl xpman-api`

Expected: all tests pass, including the five `CifpInstallableTypeTest` methods. No test executes the install flow, so no network access is required.

- [ ] **Step 5: Commit**

```bash
git add xpman-api/src/main/java/com/ogerardin/xplane/navdata/CifpManager.java
git commit -m "feat: install converted CIFP data into Custom Data"
```

---

## Task 5: Manual verification against a real X-Plane install and the real converter

These are the three checks the design doc listed under "Verification to perform during implementation". They cannot be automated without a live X-Plane install and network access, and they gate confidence in the design's assumptions.

**Files:** none modified. Record the findings.

- [ ] **Step 1: Verify the Dropbox archive layout**

Confirm the archive nests under an unknown top-level folder and that the `<os>` subtrees are as expected. Run this from a scratch directory (it downloads a real archive):

```bash
cd "$(mktemp -d)"
curl -sSL -o converter.zip \
  "https://www.dropbox.com/scl/fo/mnw9cufqcxgmkzpx35269/AG84gKEZWlR1Sk5Vld0csGk?rlkey=udqtjnhsdo0c7cnhbe2o0ft6o&dl=1"
unzip -l converter.zip
```

Expected: entries laid out as `<something>/mac/convert424toxplane`, `<something>/windows/convert424toxplane`, `<something>/windows/convert424toxplane11.exe`, `<something>/linux/convert424toxplane`, and a `<something>/windows/geoids/` subtree. If the layout differs (for example the binaries are at the archive root, or there is no `geoids` folder), update `findOsRoot`/`osFolderName` accordingly and note the change.

- [ ] **Step 2: Verify the converter's output shape**

Download a current FAA CIFP cycle from
`https://www.faa.gov/air_traffic/flight_info/aeronav/digital_products/cifp/download/`,
unpack it, and run the converter by hand in a scratch directory:

```bash
mkdir -p cifp-test && cd cifp-test
unzip -q /path/to/FAA_CIFP_cycle.zip FAACIFP18
mv FAACIFP18 FAACIFP18.dat
/path/to/extracted/mac/convert424toxplane FAACIFP18.dat "FMS"
ls -la
```

Expected: a `CIFP/` folder appears in the working directory alongside `FAACIFP18.dat`, and the process exits 0. The install flow's `copyConvertedData` assumes exactly this. If the converter instead writes to another location or a different folder name, update the constant in `copyConvertedData` and re-run `mvn test -pl xpman-api`.

- [ ] **Step 3: Verify the macOS converter architecture**

```bash
file /path/to/extracted/mac/convert424toxplane
```

Expected: a Mach-O universal binary (`arm64` and `x86_64`) or at least one that matches the development machine. If it is **arm64-only**, Intel Macs are unsupported by this path; add an `osFolderName`/binary-name branch keyed on `Platform.getCpuType()` (already available, `Platform.java:23`) and extend the "not runnable" error message to name the CPU type.

- [ ] **Step 4: Verify the full install through the wizard**

Point XPman at the FAA cycle ZIP through the existing generic install wizard and confirm:

1. The archive is identified as `FAA CIFP (ARINC 424)`.
2. On the first run, the converter is downloaded into `XPlane/Resources/tools/<os>/` and the progress console shows the download and conversion output.
3. On a second run with the same archive, no download occurs (the cached binary is reused).
4. `Custom Data/earth_424.dat` exists and matches the source file.
5. `Custom Data/CIFP/` exists and is populated.
6. The navdata screen lists the `earth_424.dat` (the `simWideOverride()` entry, `NavDataManager.java:62-88`).

- [ ] **Step 5: Verify the failure paths**

1. Temporarily point `CONVERTER_URL` at an unreachable host, run the install, and confirm the wizard reports an aborting `InstallationException` naming the download, and that no `earth_424.dat` is written.
2. Restore the URL, then temporarily make the converter non-executable (or, on macOS, re-add a quarantine attribute) and confirm the wizard aborts with a message naming the platform and X-Plane version.
3. Confirm that neither failure leaves a partial `earth_424.dat`.

- [ ] **Step 6: Record findings in the spec**

Append a "Verified on <date>" note under the design doc's "Verification to perform during implementation" section stating the actual Dropbox layout, the converter's output folder name, and the mac binary's architecture. If any of the three differ from the design's assumptions, amend the design doc too.

```bash
git add docs/superpowers/specs/2026-09-30-cifp-install-type-design.md
git commit -m "docs: record CIFP converter verification findings"
```

---

## Task 6: Correct the design doc's two inconsistencies

The design doc contradicts itself in two places. Both are documentation-only and neither changes the design, but leaving them makes the doc misleading to the next reader.

**Files:**
- Modify: `docs/superpowers/specs/2026-09-30-cifp-install-type-design.md:34,38,85-90`

- [ ] **Step 1: Fix the "New files" decision row**

Replace line 38:

```
| New files | One class `CifpInstallableType` + one test. No changes to the wizard, FXML, or existing install types |
```

with:

```
| New files | `CifpInstallableType` (adapter), `CifpManager` (filesystem + process work), one test, and a manager field on `XPlane`. No changes to the wizard, FXML, or existing install types |
```

- [ ] **Step 2: Fix the cache-location claim**

Replace line 34:

```
| Cache location | `XPlane/Resources/tools/<os>/…`, so it survives both re-installs and X-Plane directory re-creation. This is the folder `ToolUtils` already targets for external tools |
```

with:

```
| Cache location | `XPlane/Resources/tools/<os>/…`, so it survives repeated CIFP installs and Custom Data replacement. It does **not** survive deleting the X-Plane folder, since it lives inside it. This is the same folder XPman already uses for external tools |
```

- [ ] **Step 3: Correct the `ToolUtils` reference**

Replace the "Utilities" row so it no longer claims `ToolUtils.installFromZip` is used, and record why in the converter-resolution section. Replace line 39:

```
| Utilities | Reuse `ToolUtils.installFromZip` (download → temp → selective extract), `CommandExecutor` (process + output capture), `Platform` (OS detection, runnability, macOS quarantine) |
```

with:

```
| Utilities | Reuse `CommandExecutor` (process + output capture) and `Platform` (OS detection, runnability, macOS quarantine). `ToolUtils.installFromZip` is **not** used: it filters entries by their leading path elements, so it cannot extract a subtree whose parent folder name is only known after downloading |
```

Replace the "Converter resolution" step 3 bullet (lines 85-90) with:

```
   - Source is the Dropbox folder root, fetched as a single ZIP by appending `dl=1`:
     `https://www.dropbox.com/scl/fo/mnw9cufqcxgmkzpx35269/AG84gKEZWlR1Sk5Vld0csGk?rlkey=udqtjnhsdo0c7cnhbe2o0ft6o&dl=1`
   - `CifpManager` downloads the ZIP to its own temp file and reads the entry names with
     `ZipArchive.getPaths()`, locating the `<os>` subtree root as the parent of the first
     `<os>/convert424toxplane*` entry. It then extracts entries under that root, which
     strips the unknown Dropbox prefix. `ToolUtils.installFromZip` cannot do this because
     it filters on leading path elements before the prefix is known.
   - Extracting the whole `<os>` subtree keeps `geoids` alongside the Windows executable.
```

- [ ] **Step 4: Commit**

```bash
git add docs/superpowers/specs/2026-09-30-cifp-install-type-design.md
git commit -m "docs: correct CIFP design doc inconsistencies"
```

---

## Verified while writing this plan

The Java in Tasks 1-4 is not speculative. An earlier revision was applied to a scratch working
tree and run through the real Maven build, then reverted; the numbers below are from that
pass, and the code has since been revised (see the caveat afterwards):

- `mvn -B -DskipTests clean package -pl xpman-api` → **BUILD SUCCESS**
- `mvn -B test -pl xpman-api -Dtest=CifpInstallableTypeTest` → **Tests run: 5, Failures: 0, Errors: 0**
- `mvn -B test -pl xpman-api` → **Tests run: 131, Failures: 0, Errors: 0, Skipped: 10**
  (the 10 skips are the pre-existing `@EnableOnLocalXPlane` gates)

That exercise found and fixed seven defects now reflected above: the
`InstallationException` constructor mismatch, `Platforms.getCurrent()` returning `Platform`
rather than the enum, the `getCause()` access-modifier clash, the missing `install.types`
export, the mis-numbered task steps, the missing `XPlaneMajorVersion` import in the platform
classes, and the now-unused `CONVERTER` constant in `findOsRoot`.

These results were re-confirmed after the final revisions (subpath extraction,
`cifpConverterName`, nested-`FAACIFP18` handling, `getCifpManager()` delegation), applying the
plan verbatim from a clean tree. `mvn -B -DskipTests clean package` (all three modules) also
returns **BUILD SUCCESS**, confirming the new `Platform` methods and the `install.types` test
export do not break `xpman-fx` or `xpman-fx-dist`.

Two further defects surfaced in that re-check and are fixed in the plan: the
`XPlaneMajorVersion` import is needed in each platform class, and `findOsRoot` needs a
`CONVERTER_STEM` constant because the platform hook owns the exact filenames now.

The install flow (Task 4) is compile-verified only and is not exercised by any test — it
needs the live network and X-Plane access covered by Task 5.

## Full verification before declaring done

Run after Task 6:

```bash
mvn -B -DskipTests clean package
mvn test
```

Expected: BUILD SUCCESS on the full three-module build, and all `xpman-api` tests pass. The `xpman-fx` and `xpman-fx-dist` modules build unchanged — `CifpInstallableType` is discovered at runtime, so no module descriptor, FXML, or resource changes are involved.
