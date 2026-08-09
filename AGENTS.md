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

- Many xpman-api tests require `XPMAN_XPLANE_ROOT` env var pointing to an X-Plane installation. Tests annotated with `@DisabledIfNoXPlaneRootFolder` skip when it's unset.
- There is **no lint** or **typecheck** step beyond the compiler. The project previously had SonarCloud/Codacy but SonarCloud has been removed.

## Architecture

**3 Maven modules (build order):**

| Module | JPMS Module | Purpose |
|---|---|---|
| `xpman-api` | `xpman.api` | Pure Java API — domain model, file parsers, inspection framework, install logic |
| `xpman-fx` | `xpman.fx` | JavaFX desktop UI — FXML views, controllers, wizards, custom cell factories |
| `xpman-fx-dist` | *(none)* | Distribution packaging — repackaged uber-jar → platform installers (.dmg, .exe, .deb, .rpm) |

- **Java 17** source/target.
- **JPMS** is enforced — both xpman-api and xpman-fx have `module-info.java`. No automatic modules.
- **No Spring DI container.** `XPlane(folder)` is the root object that manually constructs all managers. `spring-expression` exists only for SpEL evaluation in cell factories. The Spring Boot Maven plugin is used only for repackaging into an uber-jar (`JarLauncher`), NOT for a Spring app.
- **Lombok** is heavily used: `@Data`, `@Slf4j`, `@Getter(lazy=true)`, `@SneakyThrows`, `@Delegate`, `@Builder`, `@UtilityClass`.
- **Gson** for JSON (not Jackson). User config persisted to `~/XPManPrefs.json`.
- **FXML** for all UI views — controllers follow naming convention matching the FXML file. Resource root is `xpman-fx/src/main/resources/fxml/`.
- `IntrospectionHelper.getBestSubclassInstance()` uses ClassGraph to scan the classpath for specialized domain subclasses at runtime (e.g. `ZiboMod738 extends Aircraft`).

## Architecture Patterns

- **Manager pattern**: Every domain aggregate (aircraft, plugins, scenery, navdata, tools) has a `Manager<T>` base with lazy loading, event dispatch (LOADING/LOADED), and `reload()`.
- **Inspection framework**: `Inspection<T>` is a functional interface composed via `.and()`. Domain objects implement `Inspectable`.
- **InstallSource/InstallTarget**: Strategy pattern for archive-based installation with auto-detection of archive content type.
- **Custom event system**: Lightweight `EventDispatcher<E>` with `EventListener<E>` — no framework.
- **Sealed classes**: `Tool` uses `permits InstallableTool, InstalledTool`.

## JPMS Notes

- `xpman-fx` opens packages to `javafx.base`, `javafx.fxml`, `spring.expression`, and `com.google.gson` for reflection access.
- Runtime `--add-opens` needed: see `xpman-fx-dist/xpman.l4j.ini` for ControlsFX compatibility.

## CI & Dependencies

- **CircleCI** builds on Linux, macOS, and Windows (see `.circleci/config.yml`).
- **Dependabot** manages version bumps (labels: `dependencies`, `java`).
- pecoff4j dependency comes from **Jitpack** repository (needed for reading Windows PE executables on non-Windows platforms).

## Testing

- **JUnit Jupiter 5** + **Hamcrest** + **Mockito** (xpman-api only).
- `TimingExtension` logs test method execution times.
- Tests that need a real X-Plane install use `DisabledIfNoXPlaneRootFolder` to skip automatically when `XPMAN_XPLANE_ROOT` is not set.
- Test resources include sample X-Plane files (ACF, OBJ, scenery_packs.ini, server lists) in `xpman-api/src/test/resources/`.
