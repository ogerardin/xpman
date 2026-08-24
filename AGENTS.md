# AGENTS.md

## Build & Test Commands

```bash
# Full build (all modules, no tests — as CI does)
mvn -B -DskipTests clean package

# Run tests (only xpman-api and xpman-fx have tests; xpman-fx-dist doesn't)
mvn test

# Single-module test
mvn test -pl xpman-api

# Single test class
mvn test -pl xpman-api -Dtest=ParserTest
```

- **Installers are opt-in.** `xpman-fx-dist` skips its 8 installer-producing plugins by default (`<installers.skip>true</installers.skip>` in `xpman-fx-dist/pom.xml`). CI activates them with `-Pinstallers` (or `-Dbuild.installers=true`). Without it, the repackaged uber-jar is still produced but no `.deb/.rpm/.dmg/.pkg/.exe/.msi`.
- The build uses **Maven CI-friendly versions** (`${revision}`, local default in the root POM) — see **Versioning** below. CI always overrides it with `-Drevision` computed by `scripts/ci-version.sh`.

- Many xpman-api tests access a real X-Plane installation discovered via auto-detection of common install locations (or optionally `XPMAN_XPLANE_ROOT` env var). Tests use `@EnableOnLocalXPlane` / `@EnableOnLocalXPlane11` / `@EnableOnLocalXPlane12` / `@EnableOnAircraftPresent` / `@EnableOnSceneryPresent` to skip when requirements aren't met.
- There is **no lint** or **typecheck** step beyond the compiler. The project previously had SonarCloud/Codacy but both have been removed.

## Architecture

**3 Maven modules (build order):**

| Module | JPMS Module | Purpose |
|---|---|---|
| `xpman-api` | `xpman.api` | Pure Java API — domain model, file parsers, inspection framework, install logic |
| `xpman-fx` | `xpman.fx` | JavaFX desktop UI — FXML views, controllers, wizards, custom cell factories |
| `xpman-fx-dist` | *(none)* | Distribution packaging — repackaged uber-jar → platform installers (.dmg/.pkg, .exe/.msi, .deb/.rpm) via **jpackage**; the macOS DMG is built with **dmgbuild** |

- **Java 25** source/target (`maven.compiler.release=25`); requires JDK 25 to build.
- **JPMS** is enforced — both xpman-api and xpman-fx have `module-info.java`. Most dependencies are explicit JPMS modules; only **`petitparser-core`** is a filename-based automatic module (declared under `// filename-based automodule` in `xpman-api/module-info.java`). Zip handling uses the JDK's own `java.util.zip`.
- **No Spring DI container.** `XPlane(folder)` is the root object that manually constructs all managers. `spring-expression` (currently 7.0.8) is used only for SpEL evaluation in cell factories — it is the only SpEL consumer. The Spring Boot Maven plugin is used only for repackaging into an uber-jar (`JarLauncher`), NOT for a Spring app.
- **Lombok** is heavily used: `@Data`, `@Slf4j`, `@Getter(lazy=true)`, `@SneakyThrows`, `@Delegate`, `@Builder`, `@UtilityClass`.
- **Gson** for JSON (not Jackson). User config persisted to the dotfile `~/.xpman`.
- **FXML** for all UI views — controllers follow naming convention matching the FXML file. Resource root is `xpman-fx/src/main/resources/fxml/`.
- `IntrospectionHelper.getBestSubclassInstance()` uses ClassGraph to scan the classpath for specialized domain subclasses at runtime (e.g. `ZiboMod738 extends Aircraft`).

## Architecture Patterns

- **Manager pattern**: Every domain aggregate (aircraft, plugins, scenery, navdata, tools) has a `Manager<T>` base with lazy loading, event dispatch (LOADING/LOADED), and `reload()`.
- **Inspection framework**: `Inspection<T>` is a functional interface composed via `.and()`. Domain objects implement `Inspectable`.
- **InstallSource/InstallTarget**: Strategy pattern for archive-based installation with auto-detection of archive content type.
- **Custom event system**: Lightweight `EventDispatcher<E>` with `EventListener<E>` — no framework.
- **Platform polymorphism**: The `Platform` interface is extended with default methods for platform-specific behavior (e.g. `getCandidateInstallBaseFolders`); each `MacPlatform`/`WindowsPlatform`/`LinuxPlatform` overrides as needed. Always prefer adding a method to the `Platform` interface over `if/else` on platform type.

## JPMS Notes

- `xpman-fx` opens packages to `javafx.base`, `javafx.fxml`, `spring.expression`, and `com.google.gson` for reflection access.
- `com.ogerardin.xpman.scenery_organizer` is opened **unqualified** (to all modules, including the unnamed module) so SpEL's `ReflectivePropertyAccessor` can `setAccessible` on `LibrarySceneryClass` when running under IntelliJ's classpath layout (where Spring jars land in the unnamed module). A qualified `opens ... to spring.expression` is insufficient there.
- The runtime `--add-opens=javafx.graphics/javafx.scene=org.controlsfx.controls` (ControlsFX compatibility) is passed via the jpackage `<javaOptions>` in `xpman-fx-dist/pom.xml` (per-OS profile). The legacy `xpman.l4j.ini` was removed with the launch4j migration.
- **IDE dev runs need the same flag manually**: ControlsFX's `Wizard.readSettings()` reflectively calls `Parent.getChildren()`, so an IntelliJ (or plain `java`) modular launch of `com.ogerardin.xpman.XPmanFX` must add `--add-opens=javafx.graphics/javafx.scene=org.controlsfx.controls` to its VM options — otherwise navigating a wizard fails with `InaccessibleObjectException`. jpackage builds already include it.

## macOS DMG (dmgbuild)

- The macOS DMG is built with **dmgbuild** (a Python tool), **not** jpackage: the mac profile's `dmgbuild` exec-maven-plugin (phase `package`) runs `xpman-fx-dist/assets/mac/dmgbuild/build-dmg.sh` from the `.app` image produced by jpackage. The build is **strict** — if dmgbuild fails, the Maven build fails.
- `build-dmg.sh` creates a Python venv at `xpman-fx-dist/target/.dmgbuild-venv` (PEP 668 blocks system pip) and installs dmgbuild there on first run; it finds Homebrew Pythons (`python3.14`…`python3.10`) or `/usr/local/bin/python3.{13,12}`.
- DMG appearance is defined by `assets/mac/dmgbuild/settings.py` and reproduced **pixel-perfect** from the old jpackage DMG (window rect, icon grid positions, icon/text size, background, volume icon). `background.tiff` and `VolumeIcon.icns` are committed byte-identical artifacts.
- `settings.py` is `exec`'d by dmgbuild with the options dict as its namespace: `defines` (from `-D key=value` on the CLI) is available, but **`__file__` is NOT** — always use `defines["SCRIPT_DIR"]` for paths, and `build-dmg.sh` must pass `-D SCRIPT_DIR="${SCRIPT_DIR}"`.
- Do **not** add an `@2x` background sibling next to `background.tiff` — dmgbuild would invoke `tiffutil` and produce a different file than the committed one.

## Versioning

- **Tag-based, git-authoritative.** A Git tag `X.Y.Z` (strict semver) pushed to `main` is the version. No tag → a snapshot `X.Y.(Z+1)-SNAPSHOT` where `X.Y.Z` is the highest semver tag (fallback base `1.0.0` → `1.0.1-SNAPSHOT`).
- **`scripts/ci-version.sh`** is the single source of truth: given an optional tag argument it prints/`export`s `REVISION` and `IS_SNAPSHOT`. It can be executed or sourced, but sourcing requires **bash** (uses `BASH_SOURCE`; fails under zsh — CI uses bash). It uses `git tag --sort=version:refname`, so it needs a checkout with tags (`fetch-depth: 0` on GHA).
- **Maven CI-friendly versions**: the root POM defines the local default `<revision>` (currently `1.0.1-SNAPSHOT`); all modules and inter-module dependencies reference `${revision}`. CI always overrides with `-Drevision="$REVISION"` — the POM default is only used for local builds.
- **Releases**: on a semver-tag push, GitHub Actions publishes a **release** (tag `X.Y.Z`); on every tag-less `main` push it publishes the same snapshot as a **pre-release** (`--prerelease`, delete-then-recreate). Snapshot installer artifacts get `-SNAPSHOT` inserted before the extension (`.deb/.rpm/.dmg/.pkg/.exe/.msi`); the repackaged jars already carry `-SNAPSHOT` via `finalName`.
- **Post-release bump**: after tagging `X.Y.Z`, bump the root POM `<revision>` to `X.Y.(Z+1)-SNAPSHOT` in a follow-up commit.

## CI & Dependencies

- **GitHub Actions** is the sole CI and release publisher (see `.github/workflows/build.yml`), building Linux, macOS, and Windows on every `push` (any branch or tag) and on `pull_request`. The `release` job only fires on `main` pushes and semver-tag pushes (`refs/heads/main` or `refs/tags/*`): on every tag-less `main` push it creates/recreates the GitHub release: the `release` job (after `linux`/`windows`/`mac`) downloads the artifacts, computes the version via `scripts/ci-version.sh` (no JDK 25 needed there), and runs `gh release create "$VERSION" ... --generate-notes` with delete-then-recreate semantics (`--cleanup-tag`, tag = `$VERSION`, same as the old `ghr -delete`); snapshots additionally get `--prerelease`. All CI build steps pass `-Drevision="$REVISION"` (computed by the script from `$GITHUB_REF_NAME`) and `-Pinstallers`, and checkouts use `fetch-depth: 0` so `git tag` works. Runner toolchains (Maven, JDK 25, ImageMagick, Inno Setup, WiX, Python) are pre-installed on the images; JDK 25 is wired via runner env vars (`JAVA_HOME_25_X64` / `JAVA_HOME_25_arm64`) written to `GITHUB_ENV`/`GITHUB_PATH` (the `env` context does not expose them). Never use `[skip ci]`/`[ci skip]` markers: they skip **all** CIs.
- **Dependabot** manages version bumps (labels: `dependencies`, `java`).
- pecoff4j dependency comes from **Jitpack** repository (needed for reading Windows PE executables on non-Windows platforms).

## Testing

- **JUnit Jupiter 5** + **Hamcrest** + **Mockito** (xpman-api only).
- `TimingExtension` logs test method execution times.
- Tests that need a real X-Plane install use `@EnableOnLocalXPlane*` annotations (in `com.ogerardin.test.util`) to skip automatically when no matching X-Plane installation is found; `@EnableOnAircraftPresent` and `@EnableOnSceneryPresent` additionally gate on specific add-on files.
- Test resources include sample X-Plane files (ACF, OBJ, scenery_packs.ini, server lists) in `xpman-api/src/test/resources/`.
