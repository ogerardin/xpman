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

- Many xpman-api tests access a real X-Plane installation discovered via auto-detection of common install locations (or optionally `XPMAN_XPLANE_ROOT` env var). Tests use `@EnableOnLocalXPlane` / `@EnableOnLocalXPlane11` / `@EnableOnLocalXPlane12` / `@EnableOnAircraftPresent` / `@EnableOnSceneryPresent` to skip when requirements aren't met.
- There is **no lint** or **typecheck** step beyond the compiler. The project previously had SonarCloud/Codacy but SonarCloud has been removed.

## Architecture

**3 Maven modules (build order):**

| Module | JPMS Module | Purpose |
|---|---|---|
| `xpman-api` | `xpman.api` | Pure Java API — domain model, file parsers, inspection framework, install logic |
| `xpman-fx` | `xpman.fx` | JavaFX desktop UI — FXML views, controllers, wizards, custom cell factories |
| `xpman-fx-dist` | *(none)* | Distribution packaging — repackaged uber-jar → platform installers (.dmg/.pkg, .exe/.msi, .deb/.rpm) via **jpackage** |

- **Java 25** source/target (`maven.compiler.release=25`); requires JDK 25 to build.
- **JPMS** is enforced — both xpman-api and xpman-fx have `module-info.java`. Most dependencies are explicit JPMS modules; **`commons-configuration`, `commons-lang`, `petitparser-core`, and `zip4j` are filename-based automatic modules** (declared under `// filename-based automodules` in `xpman-api/module-info.java`).
- **No Spring DI container.** `XPlane(folder)` is the root object that manually constructs all managers. `spring-expression` (currently 7.0.8) is used only for SpEL evaluation in cell factories — it is the only SpEL consumer. The Spring Boot Maven plugin is used only for repackaging into an uber-jar (`JarLauncher`), NOT for a Spring app.
- **Lombok** is heavily used: `@Data`, `@Slf4j`, `@Getter(lazy=true)`, `@SneakyThrows`, `@Delegate`, `@Builder`, `@UtilityClass`.
- **Gson** for JSON (not Jackson). User config persisted to `~/XPManPrefs.json`.
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

## CI & Dependencies

- **CircleCI** builds on Linux, macOS, and Windows (see `.circleci/config.yml`).
- **Dependabot** manages version bumps (labels: `dependencies`, `java`).
- pecoff4j dependency comes from **Jitpack** repository (needed for reading Windows PE executables on non-Windows platforms).

## Testing

- **JUnit Jupiter 5** + **Hamcrest** + **Mockito** (xpman-api only).
- `TimingExtension` logs test method execution times.
- Tests that need a real X-Plane install use `@EnableOnLocalXPlane*` annotations (in `com.ogerardin.test.util`) to skip automatically when no matching X-Plane installation is found; `@EnableOnAircraftPresent` and `@EnableOnSceneryPresent` additionally gate on specific add-on files.
- Test resources include sample X-Plane files (ACF, OBJ, scenery_packs.ini, server lists) in `xpman-api/src/test/resources/`.
