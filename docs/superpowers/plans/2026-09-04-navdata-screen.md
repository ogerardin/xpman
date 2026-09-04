# Nav Data Screen Redesign — Implementation Plan

- **Date**: 2026-09-04
- **Status**: Draft for review
- **Spec**: `docs/superpowers/specs/2026-09-04-navdata-screen-design.md` (approved)
- **Mode**: ponytail full — shortest diff, reuse existing frameworks, no new abstractions

## Context

The nav data panel (`navdata.fxml` + `NavDataController`) is currently a 5-column
`TreeTableView` with `WebViewStage` HTML popups and a PNG info icon. Replace it with a
vertical card stack mirroring the aircraft panel (`AircraftsController` +
`AircraftCardView`): one card per `NavDataSet` (6 sets from `NavDataManager`), layer
badge, inspection status, expandable file rows (size / last-modified / AIRAC cycle),
per-type feather icons, annotation-driven hover actions + context menu, and a
WebKit-free `NavDataInfoDialog`.

### Verified facts (do not re-verify)

- `NavDataSet` is `public abstract class NavDataSet extends XPlaneObject implements Inspectable, NavDataItem`
  (`NavDataSet.java:24`) with a stub `inspect()` at line 76 to replace. Ctor:
  `(String name, String description, XPlane xPlane, Path folder, String... fileNames)`.
  `getChildren()` = files + extraChildren; `getPath()` = folder; set-level
  `getExists()` = any file exists (`NavDataSet.java:94`).
- `NavDataItem` has `default String getAiracCycle() { return null; }` — **no interface
  change needed**. `NavDataFile` overrides it (parsed dat header); `CIFPSummary`,
  `AirspaceFile`, `AtcFile` inherit null. All impls override `getPath()`
  (`NavDataFile.getPath()` = `getFullPath()`).
- `Severity` enum: `INFO`, `WARN`, `ERROR` (spec says "WARNING" → use `WARN`).
- `InspectionMessage`: Lombok `@Data @Builder` — no static `of()`.
  `InspectionResult`: `of(msg)`, `of(List)`, `empty()`, `append()`, plus `@Data`-generated
  `getMessages()`.
- `XPlane(Path)` ctor only checks `Files.isDirectory` → **hermetic tests with `@TempDir`
  are valid** (`new XPlane(tempRoot)` → variant UNKNOWN, managers are lazy).
- Valid minimal `.dat` fixture (origin line + single header line, per `DatFileParser.DatHeader()`):
  ```
  I
  1100 version - data cycle 2004
  ```
- `ManagerItemsObservableList(xPlaneProperty, managerGetter, mapper)` +
  `getLoadingProperty()` + `reload()`; LOADING/LOADED events handled internally.
- `EmptyState(iconLiteral, message)` and `EmptyState.loading(message)`.
- CSS semantic vars exist in `xpman.css`: `-color-success-fg`, `-color-warning-emphasis`,
  `-color-danger-fg`, `-color-accent-subtle`, `-color-border-subtle`, `-color-bg-subtle`,
  `-color-border-muted`, `-color-fg-muted`.
- Deletions are safe (grep-verified sole usages):
  - `NavDataGroup.java` — only old `NavDataController`
  - `NavDataTableTreeItemCellFactory.java` — only `navdata.fxml`
  - `WebViewStage.java` — only the cell factory above
  - `/img/dialog-information.png` — only the cell factory (grep before deleting)
- Controllers are built via `XPmanFX.buildController` (`XPmanFX.java:247,287`); current
  `NavDataController(XPmanFX)` ctor (line 42) stays.
- Menu/action framework annotations: `com.ogerardin.xpman.util.jfx.menu.annotation.*`
  (`@Label`, `@EnabledIf`, `@OnSuccess`). `UiPlugin.inspect()` is the pattern to copy.
- `GenericContextMenuFactory` + `MethodButton` + `MethodActionConfigurer` +
  `IntrospectionHelper.computeRelevantMethods` — copy exact imports from
  `AircraftCardView.java` (lines 1–40).
- Out of scope (spec): search/filter box, writing/toggling nav data files, any
  `DatFile` subsystem change. No new fx tests.

## Task 1 — API: `NavDataSet.inspect()` (TDD)

**Files**
- NEW `xpman-api/src/test/java/com/ogerardin/xplane/navdata/NavDataSetTest.java`
  (same package as class under test — no ctor-visibility fights)
- MODIFY `xpman-api/src/main/java/com/ogerardin/xplane/navdata/NavDataSet.java` (replace
  stub `inspect()` at ~line 76)

**1.1 — Write the test (red)**

⚠️ Do NOT resolve the data folder in a field initializer — field initializers run
before `@TempDir` injection. Use a method-local folder, as below.

```java
package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Hermetic tests for {@link NavDataSet#inspect()}: missing files and AIRAC cycle
 * consistency of the set's data files.
 */
class NavDataSetTest {

    private static final String DAT_WITH_CYCLE = "I\n1100 version - data cycle %s\n";

    @TempDir
    Path xplaneRoot;

    private NavDataSet dataSet(Path dataFolder, String... fileNames) throws Exception {
        return new NavDataSet("Test set", "test", new XPlane(xplaneRoot), dataFolder, fileNames) {};
    }

    private static void writeDat(Path folder, String fileName, String cycle) throws Exception {
        Files.createDirectories(folder);
        Files.writeString(folder.resolve(fileName), DAT_WITH_CYCLE.formatted(cycle));
    }

    @Test
    void reportsMissingFilesAndCurrentCycle() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2004");

        InspectionResult result = dataSet(data, "earth_nav.dat", "earth_fix.dat").inspect();

        assertThat(result.getMessages(), hasItem(hasProperty("message",
                is("File not found: earth_fix.dat"))));
        assertThat(result.getMessages(), hasItem(hasProperty("message",
                is("OK — cycle 2004"))));
    }

    @Test
    void warnsOnMixedCycles() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2004");
        writeDat(data, "earth_fix.dat", "1905");

        InspectionResult result = dataSet(data, "earth_nav.dat", "earth_fix.dat").inspect();

        assertThat(result.getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.WARN)),
                hasProperty("message", is("Mixed AIRAC cycles: 1905, 2004")))));
    }

    @Test
    void infoWhenNoData() throws Exception {
        InspectionResult result = dataSet(xplaneRoot.resolve("Custom Data")).inspect();

        assertThat(result.getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.INFO)),
                hasProperty("message", is("No data present")))));
    }
}
```

**1.2 — Red**: `mvn test -pl xpman-api -Dtest=NavDataSetTest` → compiles, tests fail
against the stub. (If a cycle assertion fails with "No data present", the fixture
format drifted from the parser — fix the fixture, not the parser.)

**1.3 — Implement (green)**: replace `NavDataSet.inspect()` with:

```java
/**
 * Inspects the data files of this set: reports missing files as errors, and warns
 * when the existing files carry inconsistent AIRAC cycles.
 */
@Override
public InspectionResult inspect() {
    List<InspectionMessage> messages = getChildren().stream()
            .filter(item -> !item.getExists())
            .map(item -> InspectionMessage.builder()
                    .severity(Severity.ERROR)
                    .message("File not found: " + item.getName())
                    .build())
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> cycles = getChildren().stream()
            .filter(NavDataItem::getExists)
            .map(NavDataItem::getAiracCycle)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList();

    Severity severity = Severity.INFO;
    String message = switch (cycles.size()) {
        case 1 -> "OK — cycle " + cycles.get(0);
        case 0 -> "No data present";
        default -> {
            severity = Severity.WARN;
            yield "Mixed AIRAC cycles: " + String.join(", ", cycles);
        }
    };
    messages.add(InspectionMessage.builder().severity(severity).message(message).build());

    return InspectionResult.of(messages);
}
```

Add imports: `com.ogerardin.xplane.inspection.InspectionMessage`,
`com.ogerardin.xplane.inspection.Severity`, `java.util.ArrayList`, `java.util.Objects`,
`java.util.stream.Collectors` (`InspectionResult`/`Inspectable` already imported).
Note: `getExists()` returns `Boolean` (non-null in all impls) — stream filter
auto-unboxes.

**1.4 — Verify**: `mvn test -pl xpman-api` (full module — no regressions).

**1.5 — Commit**: `Real NavDataSet.inspect(): missing files + AIRAC cycle consistency, with hermetic tests`

## Task 2 — FX: `UiNavDataItem`, `NavDataInfoDialog`, `NavDataSetCardView`

New classes compile standalone (unused until Task 3). Copy action-framework imports
from `AircraftCardView.java`.

**2.1 — `UiNavDataItem.java`** (modify): keep `@Data`, `@ToString(includeFieldNames = false)`,
`@RequiredArgsConstructor`, `@Delegate final NavDataItem navDataItem`, existing
`reveal()` (with its `@Label(...revealLabel())` + `@EnabledIf("exists")`). **Delete the
no-arg ctor** (grep-verified unused outside the class). Add:

```java
@OnSuccess("displayInspectionResults(#result)")
public InspectionResult inspect() {
    return navDataItem instanceof Inspectable inspectable
            ? inspectable.inspect()
            : InspectionResult.empty();
}
```

Imports: `com.ogerardin.xplane.inspection.{Inspectable, InspectionResult}`,
`com.ogerardin.xpman.util.jfx.menu.annotation.OnSuccess`.
(`NavDataItem` has no `inspect()` so `@Delegate` doesn't collide. Delegated
`get*()` methods are skipped by `IntrospectionHelper.computeRelevantMethods`, so the
context menu/hover actions are exactly `[reveal, inspect]`.)

**2.2 — NEW `xpman-fx/src/main/java/com/ogerardin/xpman/panels/navdata/NavDataInfoDialog.java`**:

```java
package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.util.platform.Platforms;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.Optional;

/**
 * Native (WebKit-free) info dialog for a nav data item: description (HTML stripped),
 * folder, header metadata, and a link to the X-Plane nav data documentation.
 * Replaces the old WebViewStage HTML popup.
 */
public class NavDataInfoDialog extends Dialog<Void> {

    private static final String DOC_URL = "https://developer.x-plane.com/article/navdata/";

    public NavDataInfoDialog(UiNavDataItem item, Window owner) {
        initOwner(owner);
        setTitle("Nav data: " + item.getName());

        VBox content = new VBox(8);
        content.setPadding(new Insets(12));
        content.setPrefWidth(420);

        Label description = new Label(stripHtml(item.getDescription()));
        description.setWrapText(true);
        description.getStyleClass().add("navdata-info-description");
        content.getChildren().add(description);

        Optional.ofNullable(item.getPath()).ifPresent(path -> {
            Label folder = new Label(path.toString());
            folder.setWrapText(true);
            folder.getStyleClass().add("navdata-info-path");
            content.getChildren().add(folder);
        });

        VBox details = new VBox(2);
        addDetail(details, "AIRAC cycle", item.getAiracCycle());
        addDetail(details, "Metadata", item.getMetadata());
        addDetail(details, "Build", item.getBuild());
        if (!details.getChildren().isEmpty()) {
            content.getChildren().add(details);
        }

        Hyperlink docLink = new Hyperlink("X-Plane nav data documentation");
        docLink.setOnAction(__ -> openDoc());
        content.getChildren().add(docLink);

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    }

    public static void show(UiNavDataItem item, Node card) {
        Window owner = card.getScene() != null ? card.getScene().getWindow() : null;
        new NavDataInfoDialog(item, owner).showAndWait();
    }

    @SneakyThrows
    private static void openDoc() {
        Platforms.getCurrent().openUrl(new URL(DOC_URL));
    }

    private static void addDetail(VBox details, String label, String value) {
        if (value != null && !value.isBlank()) {
            details.getChildren().add(new Label(label + ": " + value));
        }
    }

    private static String stripHtml(String html) {
        String stripped = html == null ? "" : html.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
        return stripped.isBlank() ? "No description available." : stripped;
    }
}
```

**2.3 — NEW `xpman-fx/src/main/java/com/ogerardin/xpman/panels/navdata/NavDataSetCardView.java`**:

```java
package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.navdata.NavDataItem;
import com.ogerardin.xpman.util.IntrospectionHelper;
import com.ogerardin.xpman.util.jfx.menu.GenericContextMenuFactory;
import com.ogerardin.xpman.util.jfx.menu.MethodButton;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Card for one nav data set: header with layer badge, help button and inspection
 * status, plus an expandable per-file list. Hover actions and the context menu come
 * from the annotation-driven action framework (see AircraftCardView).
 */
public class NavDataSetCardView extends VBox {

    private static final int ICON_SIZE = 14;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Icon per nav data set, keyed by display name (see NavDataManager.loadNavDataSets()). */
    private static final Map<String, Feather> SET_ICONS = Map.of(
            "Sim-wide override", Feather.GLOBE,
            "Base data", Feather.DATABASE,
            "Updated base data", Feather.REFRESH_CW,
            "FAA updated approaches", Feather.FLAG,
            "Hand-placed localizers", Feather.MAP_PIN,
            "User data", Feather.USER);

    /** Icon per data file, keyed by leaf name ("CIFP" = directory of CIFPSummary). */
    private static final Map<String, Feather> FILE_ICONS = Map.ofEntries(
            Map.entry("earth_nav.dat", Feather.NAVIGATION),
            Map.entry("earth_fix.dat", Feather.MAP_PIN),
            Map.entry("earth_awy.dat", Feather.ROUTE),
            Map.entry("earth_hold.dat", Feather.ANCHOR),
            Map.entry("earth_mora.dat", Feather.LAYERS),
            Map.entry("earth_424.dat", Feather.GLOBE),
            Map.entry("FAACIFP18", Feather.AIRPLAY),
            Map.entry("user_nav.dat", Feather.RADIO),
            Map.entry("user_fix.dat", Feather.RADIO),
            Map.entry("CIFP", Feather.FOLDER),
            Map.entry("airspace.txt", Feather.FILE_TEXT),
            Map.entry("atc.dat", Feather.FILE_TEXT));

    private final NavDataController controller;
    private final GenericContextMenuFactory<UiNavDataItem> menuFactory;

    private VBox filesBox;
    private FontIcon filesChevron;

    public NavDataSetCardView(UiNavDataItem uiItem, int layerIndex, int layerCount, NavDataController controller) {
        this.controller = controller;
        this.menuFactory = controller.getCardMenuFactory();

        getStyleClass().add("navdata-card");

        Label nameLabel = new Label(uiItem.getName(), icon(SET_ICONS.getOrDefault(uiItem.getName(), Feather.DATABASE)));
        nameLabel.getStyleClass().add("navdata-card-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        Label layerBadge = new Label("Layer " + layerIndex + "/" + layerCount);
        layerBadge.getStyleClass().add("navdata-card-badge");

        Label statusLabel = buildStatusLabel(uiItem);

        Button helpButton = new Button();
        helpButton.setGraphic(icon(Feather.HELP_CIRCLE));
        helpButton.getStyleClass().add("navdata-card-help");
        helpButton.setOnAction(__ -> NavDataInfoDialog.show(uiItem, this));

        HBox header = new HBox(8, nameLabel, layerBadge, statusLabel, helpButton);
        header.getStyleClass().add("navdata-card-header");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Button filesToggle = new Button(uiItem.getChildren().size() + " files");
        filesChevron = new FontIcon(Feather.CHEVRON_DOWN);
        filesChevron.setIconSize(ICON_SIZE);
        filesToggle.setGraphic(filesChevron);
        filesToggle.getStyleClass().add("navdata-card-files-toggle");
        filesToggle.setOnAction(__ -> toggleFiles(uiItem));

        HBox actions = buildHoverActions(uiItem);

        VBox info = new VBox(4, header, filesToggle, actions);
        VBox.setMargin(info, new Insets(6, 8, 6, 8));
        getChildren().add(info);

        ContextMenu cardMenu = menuFactory.menuFor(uiItem);
        setOnContextMenuRequested(event -> showMenu(cardMenu, this, event));
    }

    private static Label buildStatusLabel(UiNavDataItem uiItem) {
        List<InspectionMessage> messages = uiItem.inspect().getMessages();
        // NavDataSet.inspect() always appends the summary message last
        InspectionMessage summary = messages.get(messages.size() - 1);
        Label statusLabel = new Label(summary.getMessage());
        statusLabel.getStyleClass().add("navdata-status-"
                + summary.getSeverity().toString().toLowerCase(Locale.ROOT));
        return statusLabel;
    }

    private HBox buildHoverActions(UiNavDataItem uiItem) {
        HBox actions = new HBox(4);
        actions.getStyleClass().add("navdata-card-actions");
        addAction(actions, uiItem, "inspect", Feather.ACTIVITY);
        addAction(actions, uiItem, "reveal", Feather.FOLDER);
        return actions;
    }

    private void addAction(HBox actions, UiNavDataItem uiItem, String methodName, Feather feather) {
        findMethod(uiItem.getClass(), methodName).ifPresent(method -> {
            MethodButton<UiNavDataItem> button = new MethodButton<>(methodName, method, controller, uiItem);
            button.setText(null);
            button.setGraphic(icon(feather));
            button.getStyleClass().add("navdata-card-action");
            actions.getChildren().add(button);
        });
    }

    private static Optional<Method> findMethod(Class<?> type, String name) {
        return IntrospectionHelper.computeRelevantMethods(type).stream()
                .filter(method -> method.getName().equals(name) && method.getParameterCount() == 0)
                .findFirst();
    }

    private void toggleFiles(UiNavDataItem uiItem) {
        if (filesBox != null) {
            boolean expanded = !filesBox.isVisible();
            filesBox.setVisible(expanded);
            filesBox.setManaged(expanded);
            filesChevron.setRotate(expanded ? 90 : 0);
            return;
        }
        filesBox = new VBox(2);
        filesBox.getStyleClass().add("navdata-card-files");
        uiItem.getChildren().forEach(item -> filesBox.getChildren().add(buildFileRow(item)));
        getChildren().add(filesBox);
        filesChevron.setRotate(90);
    }

    private HBox buildFileRow(NavDataItem item) {
        Label nameLabel = new Label(item.getName(), icon(FILE_ICONS.getOrDefault(leafOf(item), Feather.FILE)));
        nameLabel.getStyleClass().add("navdata-card-file-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        HBox row = new HBox(8, nameLabel);
        row.getStyleClass().add("navdata-card-file-row");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        if (!item.getExists()) {
            nameLabel.getStyleClass().add("navdata-card-file-missing");
            Label missing = new Label("not found");
            missing.getStyleClass().add("navdata-status-error");
            row.getChildren().add(missing);
        } else {
            Optional.ofNullable(item.getAiracCycle()).ifPresent(cycle -> {
                Label cycleBadge = new Label("AIRAC " + cycle);
                cycleBadge.getStyleClass().add("navdata-card-badge");
                row.getChildren().add(cycleBadge);
            });
            Label meta = new Label(metaText(item));
            meta.getStyleClass().add("navdata-card-file-meta");
            row.getChildren().add(meta);
        }
        return row;
    }

    private static String leafOf(NavDataItem item) {
        Path path = item.getPath();
        return path != null && path.getFileName() != null ? path.getFileName().toString() : "";
    }

    private static String metaText(NavDataItem item) {
        try {
            Path path = item.getPath();
            String size = humanSize(Files.size(path));
            String modified = DATE_FORMAT.format(
                    Files.getLastModifiedTime(path).toInstant().atZone(ZoneId.systemDefault()));
            return size + " · " + modified;
        } catch (IOException e) {
            return "?";
        }
    }

    private static String humanSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / 1024.0 / 1024);
        return String.format("%.1f GB", bytes / 1024.0 / 1024 / 1024);
    }

    private static FontIcon icon(Feather feather) {
        FontIcon fontIcon = new FontIcon(feather);
        fontIcon.setIconSize(ICON_SIZE);
        return fontIcon;
    }

    /**
     * Shows the given context menu over the owner node and guarantees that any mouse
     * press outside it dismisses it. Copied verbatim from AircraftCardView.
     */
    private static void showMenu(ContextMenu menu, Node owner, ContextMenuEvent event) {
        menu.setAutoHide(true);
        menu.show(owner, event.getScreenX(), event.getScreenY());
        event.consume();

        Scene scene = owner.getScene();
        if (scene == null) {
            return;
        }
        EventHandler<MouseEvent> outsidePressHandler = __ -> menu.hide();
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressHandler);
        menu.setOnHidden(__ -> scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressHandler));
    }
}
```

⚠️ Before finalizing `SET_ICONS`: check the actual display names (first ctor arg of
each set) in `NavDataManager.loadNavDataSets()` (`NavDataManager.java:85–215`) and align
the keys. `getOrDefault(..., Feather.DATABASE)` makes a wrong key harmless.

Notes:
- `buildStatusLabel` calls `inspect()` during card construction → parses `.dat` headers
  on the FX thread. This is exactly what the current tree screen does per visible row
  (`TreeItemPropertyValueFactory("airacCycle")`) — no regression, keep.

**2.4 — Verify**: `mvn -B -DskipTests clean package` (all modules compile).

**2.5 — Commit**: `Nav data UI groundwork: UiNavDataItem.inspect, card view, info dialog (unused until controller swap)`

## Task 3 — FXML + controller rewrite + deletions

**3.1 — Replace `xpman-fx/src/main/resources/fxml/panels/navdata.fxml`** (keep
AnchorPane root so the include site is untouched):

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import org.kordamp.ikonli.javafx.FontIcon?>
<AnchorPane prefHeight="444.0" prefWidth="864.0" xmlns="http://javafx.com/javafx/17.0.2-ea" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.ogerardin.xpman.panels.navdata.NavDataController">
    <BorderPane AnchorPane.bottomAnchor="0.0" AnchorPane.leftAnchor="0.0" AnchorPane.rightAnchor="0.0" AnchorPane.topAnchor="0.0">
        <top>
            <ToolBar fx:id="toolbar">
                <Button mnemonicParsing="false" onAction="#reload" text="Reload">
                    <graphic><FontIcon iconLiteral="fth-refresh-cw"/></graphic>
                </Button>
                <Button mnemonicParsing="false" onAction="#install" text="Install...">
                    <graphic><FontIcon iconLiteral="fth-download"/></graphic>
                </Button>
            </ToolBar>
        </top>
        <center>
            <StackPane>
                <ScrollPane fitToWidth="true" hbarPolicy="NEVER">
                    <VBox fx:id="cardsPane" spacing="12" styleClass="navdata-cards"/>
                </ScrollPane>
                <StackPane fx:id="placeholderPane"/>
            </StackPane>
        </center>
    </BorderPane>
</AnchorPane>
```

(placeholderPane is last → on top; toggled visible/managed by the controller.)

**3.2 — Rewrite `NavDataController.java`** (full file):

```java
package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.navdata.NavDataSet;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.Controller;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.util.jfx.EmptyState;
import com.ogerardin.xpman.util.jfx.menu.GenericContextMenuFactory;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for the nav data panel: a vertical stack of {@link NavDataSetCardView}
 * cards, one per {@link NavDataSet}, mirroring the aircraft panel structure.
 */
@Slf4j
public class NavDataController extends Controller {

    private final XPlaneProperty xPlaneProperty;

    @FXML
    private ToolBar toolbar;

    @FXML
    private VBox cardsPane;

    @FXML
    private StackPane placeholderPane;

    private ManagerItemsObservableList<NavDataSet, UiNavDataItem> uiItems;

    private final GenericContextMenuFactory<UiNavDataItem> cardMenuFactory =
            new GenericContextMenuFactory<>(this);

    public NavDataController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
    }

    @FXML
    public void initialize() {
        uiItems = new ManagerItemsObservableList<>(
                xPlaneProperty,
                XPlane::getNavDataManager,
                UiNavDataItem::new
        );
        uiItems.addListener((ListChangeListener<UiNavDataItem>) __ -> Platform.runLater(this::updateCards));
        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));
        updateCards();
    }

    private void updateCards() {
        cardMenuFactory.clearCache();
        cardsPane.getChildren().clear();
        for (int i = 0; i < uiItems.size(); i++) {
            cardsPane.getChildren().add(new NavDataSetCardView(uiItems.get(i), i + 1, uiItems.size(), this));
        }

        boolean loading = uiItems.getLoadingProperty().get();
        boolean empty = uiItems.isEmpty();
        placeholderPane.setVisible(empty || loading);
        placeholderPane.setManaged(empty || loading);
        placeholderPane.getChildren().setAll(loading
                ? EmptyState.loading("Loading nav data...")
                : new EmptyState("fth-navigation", "No nav data to show"));
    }

    GenericContextMenuFactory<UiNavDataItem> getCardMenuFactory() {
        return cardMenuFactory;
    }

    public void install() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.NAVDATA);
        wizard.showAndWait();
        uiItems.reload();
    }

    public void reload() {
        uiItems.reload();
    }
}
```

**3.3 — Delete** (grep first to confirm zero remaining usages after the rewrite):
- `xpman-api/src/main/java/com/ogerardin/xplane/navdata/NavDataGroup.java`
- `xpman-fx/src/main/java/com/ogerardin/xpman/panels/navdata/NavDataTableTreeItemCellFactory.java`
- `xpman-fx/src/main/java/com/ogerardin/xpman/util/jfx/WebViewStage.java`
- `xpman-fx/src/main/resources/img/dialog-information.png` (only if the grep shows no
  other usage)

**3.4 — Verify**: `mvn -B -DskipTests clean package` + `mvn test -pl xpman-api`.

**3.5 — Commit**: `Nav data panel: card stack replaces tree table; drop WebViewStage/NavDataGroup`

## Task 4 — CSS

Append to `xpman-fx/src/main/resources/css/xpman.css` (end of file). Mirror the
`.aircraft-card` block (lines ~392–485) for card/badge/actions/hover rules; copy the
`.aircraft-card-actions` hover-reveal rules verbatim with `navdata-` prefixes so hover
behavior matches the aircraft grid exactly.

```css
/* ============ Nav data cards ============ */

.navdata-cards {
    -fx-padding: 12;
}

.navdata-card {
    -fx-background-color: -color-bg-subtle;
    -fx-background-radius: 8;
    -fx-border-color: -color-border-muted;
    -fx-border-radius: 8;
    -fx-border-width: 1;
}

.navdata-card-header {
    -fx-alignment: CENTER_LEFT;
    -fx-spacing: 8;
}

.navdata-card-name {
    -fx-font-weight: bold;
}

.navdata-card-badge {
    -fx-background-color: -color-accent-subtle;
    -fx-border-color: -color-border-subtle;
    -fx-border-radius: 4;
    -fx-padding: 1 6 1 6;
}

.navdata-card-help {
    -fx-background-color: transparent;
    -fx-border-color: transparent;
    -fx-padding: 0;
}

.navdata-status-info {
    -fx-text-fill: -color-fg-muted;
}

.navdata-status-warn {
    -fx-text-fill: -color-warning-emphasis;
}

.navdata-status-error {
    -fx-text-fill: -color-danger-fg;
}

.navdata-card-files-toggle {
    -fx-background-color: transparent;
    -fx-border-color: transparent;
    -fx-padding: 0;
    -fx-text-fill: -color-fg-muted;
}

.navdata-card-file-row {
    -fx-alignment: CENTER_LEFT;
    -fx-spacing: 8;
    -fx-padding: 0 0 0 18;
}

.navdata-card-file-name {
    -fx-text-fill: -color-fg-muted;
}

.navdata-card-file-missing {
    -fx-text-fill: -color-danger-fg;
}

.navdata-card-file-meta {
    -fx-text-fill: -color-fg-muted;
}

.navdata-info-description {
    -fx-text-fill: -color-fg-muted;
}

.navdata-info-path {
    -fx-text-fill: -color-fg-muted;
    -fx-font-family: monospace;
}
```

Plus (copied from `.aircraft-card-actions`/`.aircraft-card-action` rules with the
prefix swapped — check those rules in the `.aircraft-card` block for exact
opacity/transition/padding values):

```css
.navdata-card-actions {
    -fx-alignment: CENTER_LEFT;
    -fx-spacing: 4;
    -fx-opacity: 0;
}

.navdata-card:hover .navdata-card-actions {
    -fx-opacity: 1;
}

.navdata-card-action {
    -fx-background-color: -color-bg-inset;
    -fx-border-color: -color-border-subtle;
    -fx-border-radius: 4;
}
```

**Verify**: `mvn -B -DskipTests clean package`.
**Commit**: `Nav data card styles`

## Task 5 — Final verification

1. `mvn test -pl xpman-api` — full api suite green.
2. `mvn -B -DskipTests clean package` — full build green.
3. Manual smoke (IDE run needs `--add-opens=javafx.graphics/javafx.scene=org.controlsfx.controls`):
   - Nav data tab shows 6 cards with Layer N/6 badges, set icons, status text.
   - "N files" toggle expands/collapses file rows (icon, AIRAC badge, size · date;
     missing files red).
   - Hover reveals Inspect/Reveal buttons; Inspect shows the inspection dialog
     (INFO/WARN/ERROR messages from Task 1); Reveal opens the folder (enabled only
     when set exists).
   - Right-click card → context menu [Reveal, Inspect].
   - Help button → `NavDataInfoDialog` (no WebView): description, path, metadata,
     doc hyperlink, CLOSE.
   - Reload + Install buttons behave as before (wizard + reload).
4. No fx tests to run (per spec); `NavDataSetTest` is the runnable check.

## Rollback

Each task is one commit — `git revert` per task. Task 3 is the only user-visible swap;
Tasks 1–2 are additive, Task 4 is cosmetic.
