# Settings Dialog Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** IntelliJ-style Settings dialog (hierarchic category tree left, settings pane right, OK/Cancel) with a General category ("confirm quit") and a Tools category hosting the existing tools pane.

**Architecture:** Mirrors the existing `Section`/sidebar idiom — a `SettingsCategory` enum (label + icon + FXML path), a `SettingsController` that lazily loads/caches category panes like `XPmanFX.showSection`, and a 3-line `SettingsPage` interface for OK-apply semantics. `confirmQuit` lands on `JfxAppPrefs` with a one-`if` gate in `JfxApp.quit()`. Existing `tools.fxml`/`ToolsController` reused untouched.

**Tech Stack:** JavaFX FXML, Lombok, Gson persister, AtlantaFX CSS variables, JUnit 5

**Design doc:** `docs/superpowers/specs/2026-09-29-settings-dialog-design.md`

---

### Task 1: `confirmQuit` preference + quit gate (TDD)

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/util/jfx/JfxAppPrefs.java`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/util/jfx/JfxApp.java:119-126`
- Test: `xpman-fx/src/test/java/com/ogerardin/xpman/util/test/JsonFileConfigPersisterTest.java`

- [ ] **Step 1: Write failing tests** — add to `JsonFileConfigPersisterTest` (mirroring the existing `themeDefaultsToDarkWhenMissingFromPrefs` pattern):

```java
@Test
void confirmQuitDefaultsToTrueWhenMissingFromPrefs() throws IOException {
    Path file = Files.createTempFile("XPManPrefs", ".json");
    try {
        Files.writeString(file, "{\"lastXPlanePath\":\"/X-Plane 12\"}");
        JsonFileConfigPersister<XPManPrefs> prefsManager = new JsonFileConfigPersister<>(XPManPrefs.class, file);
        assertThat(prefsManager.getConfig().isConfirmQuit(), is(true));
    } finally {
        Files.deleteIfExists(file);
    }
}

@Test
void confirmQuitRoundTrips() throws IOException {
    Path file = Files.createTempFile("XPManPrefs", ".json");
    try {
        JsonFileConfigPersister<XPManPrefs> saver = new JsonFileConfigPersister<>(XPManPrefs.class, file);
        saver.getConfig().setConfirmQuit(false);
        saver.save();

        JsonFileConfigPersister<XPManPrefs> loader = new JsonFileConfigPersister<>(XPManPrefs.class, file);
        assertThat(loader.getConfig().isConfirmQuit(), is(false));
    } finally {
        Files.deleteIfExists(file);
    }
}
```

- [ ] **Step 2: Run, verify failure** — `mvn test -pl xpman-fx -Dtest=JsonFileConfigPersisterTest` → FAIL (no `isConfirmQuit()`).
- [ ] **Step 3: Implement** — `JfxAppPrefs`, first field:

```java
boolean confirmQuit = true;
```

Gate in `JfxApp.quit()`:

```java
@FXML
protected void quit() {
    if (!getConfig().isConfirmQuit()) {
        quitNow();
        return;
    }
    Alert alert = new Alert(CONFIRMATION, "Do you really want to quit?");
    alert.initOwner(primaryStage);
    alert.showAndWait()
            .filter(buttonType -> buttonType == ButtonType.OK)
            .ifPresent(buttonType -> quitNow());
}
```

- [ ] **Step 4: Run, verify pass** — same command → PASS (4 tests).
- [ ] **Step 5: Commit** — `feat: add confirmQuit preference gating quit confirmation`

---

### Task 2: Settings framework types + General pane

**Files:**
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/settings/SettingsCategory.java`
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/settings/SettingsPage.java`
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/settings/GeneralController.java`
- Create: `xpman-fx/src/main/resources/fxml/settings/general.fxml`
- Modify: `xpman-fx/src/main/java/module-info.java`

- [ ] **Step 1: `SettingsCategory`** — same shape as `Section`:

```java
package com.ogerardin.xpman.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The categories of the Settings dialog, mapped to their entry in the category tree (label + Ikonli icon literal)
 * and the FXML view displayed in the settings pane when selected.
 */
@RequiredArgsConstructor
@Getter
public enum SettingsCategory {
    GENERAL("General", "fth-settings", "/fxml/settings/general.fxml"),
    TOOLS("Tools", "fth-tool", "/fxml/tools/tools.fxml");

    private final String label;
    private final String iconLiteral;
    private final String contentFxml;
}
```

- [ ] **Step 2: `SettingsPage`**:

```java
package com.ogerardin.xpman.settings;

/**
 * Implemented by settings pane controllers that hold editable preference state;
 * {@link #apply()} is called when the user clicks OK in the Settings dialog.
 */
public interface SettingsPage {
    void apply();
}
```

- [ ] **Step 3: `general.fxml`**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<VBox xmlns="http://javafx.com/javafx/17.0.2-ea" xmlns:fx="http://javafx.com/fxml/1"
      fx:controller="com.ogerardin.xpman.settings.GeneralController"
      styleClass="settings-pane">
    <CheckBox fx:id="confirmQuitCheckBox" text="Confirm quit"/>
</VBox>
```

- [ ] **Step 4: `GeneralController`**:

```java
package com.ogerardin.xpman.settings;

import com.ogerardin.xpman.XPmanFX;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import lombok.RequiredArgsConstructor;

/**
 * Controller of the General settings pane.
 */
@RequiredArgsConstructor
public class GeneralController implements SettingsPage {

    private final XPmanFX mainController;

    @FXML
    private CheckBox confirmQuitCheckBox;

    @FXML
    private void initialize() {
        confirmQuitCheckBox.setSelected(mainController.getConfig().isConfirmQuit());
    }

    @Override
    public void apply() {
        mainController.getConfig().setConfirmQuit(confirmQuitCheckBox.isSelected());
    }
}
```

- [ ] **Step 5: module-info** — add:

```java
opens com.ogerardin.xpman.settings to javafx.base, javafx.fxml;
```

- [ ] **Step 6: Compile** — `mvn -B -DskipTests clean package` → BUILD SUCCESS.
- [ ] **Step 7: Commit** — `feat: add SettingsCategory, SettingsPage and General settings pane`

---

### Task 3: Settings dialog view + controller

**Files:**
- Create: `xpman-fx/src/main/resources/fxml/settings/settings.fxml`
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/settings/SettingsController.java`

- [ ] **Step 1: `settings.fxml`**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<BorderPane xmlns="http://javafx.com/javafx/17.0.2-ea" xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="com.ogerardin.xpman.settings.SettingsController"
            styleClass="settings-dialog" prefWidth="800.0" prefHeight="550.0">
    <left>
        <TreeView fx:id="categoryTree" styleClass="settings-category-tree" prefWidth="220.0" showRoot="false"/>
    </left>
    <center>
        <StackPane fx:id="contentArea"/>
    </center>
    <bottom>
        <HBox styleClass="settings-button-bar">
            <Region HBox.hgrow="ALWAYS"/>
            <Button mnemonicParsing="false" onAction="#ok" text="OK" defaultButton="true"/>
            <Button mnemonicParsing="false" onAction="#cancel" text="Cancel" cancelButton="true"/>
        </HBox>
    </bottom>
</BorderPane>
```

- [ ] **Step 2: `SettingsController`** — mirrors `XPmanFX.showSection`/`sectionCache` + `SidebarController` icon idiom:

```java
package com.ogerardin.xpman.settings;

import com.ogerardin.xpman.XPmanFX;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller of the Settings dialog: hierarchic category tree on the left, settings pane on the right.
 * Panes are lazily loaded and cached; on OK, every loaded {@link SettingsPage} gets applied and the
 * config is saved.
 */
@RequiredArgsConstructor
public class SettingsController {

    private static final int ICON_SIZE = 16;

    private final XPmanFX mainController;

    @FXML
    private TreeView<SettingsCategory> categoryTree;

    @FXML
    private StackPane contentArea;

    private final Map<SettingsCategory, Node> paneCache = new EnumMap<>(SettingsCategory.class);
    private final Map<SettingsCategory, Object> controllerCache = new EnumMap<>(SettingsCategory.class);

    @FXML
    private void initialize() {
        TreeItem<SettingsCategory> root = new TreeItem<>();
        for (SettingsCategory category : SettingsCategory.values()) {
            FontIcon icon = new FontIcon(category.getIconLiteral());
            icon.setIconSize(ICON_SIZE);
            TreeItem<SettingsCategory> item = new TreeItem<>(category);
            item.setGraphic(icon);
            root.getChildren().add(item);
        }
        categoryTree.setRoot(root);
        categoryTree.getSelectionModel().selectedItemProperty().addListener((__, ___, item) ->
                Optional.ofNullable(item).map(TreeItem::getValue).ifPresent(this::showCategory));
    }

    /** Selects the given category (which displays its pane). */
    public void select(SettingsCategory category) {
        categoryTree.getRoot().getChildren().stream()
                .filter(item -> item.getValue() == category)
                .findFirst()
                .ifPresent(item -> categoryTree.getSelectionModel().select(item));
    }

    private void showCategory(SettingsCategory category) {
        Node content = paneCache.computeIfAbsent(category, this::loadPane);
        if (contentArea.getChildren().isEmpty() || contentArea.getChildren().get(0) != content) {
            contentArea.getChildren().setAll(content);
        }
    }

    @SneakyThrows
    private Node loadPane(SettingsCategory category) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(category.getContentFxml()));
        loader.setControllerFactory(mainController::buildController);
        Node pane = loader.load();
        controllerCache.put(category, loader.getController());
        return pane;
    }

    @FXML
    private void ok() {
        controllerCache.values().forEach(controller -> {
            if (controller instanceof SettingsPage settingsPage) {
                settingsPage.apply();
            }
        });
        mainController.saveConfig();
        close();
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        contentArea.getScene().getWindow().hide();
    }
}
```

- [ ] **Step 3: Compile** — `mvn -B -DskipTests clean package` → BUILD SUCCESS.
- [ ] **Step 4: Commit** — `feat: add Settings dialog view and controller`

---

### Task 4: XPmanFX wiring + menu + Section.TOOLS removal

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java`
- Modify: `xpman-fx/src/main/resources/fxml/main.fxml`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/shell/Section.java`

- [ ] **Step 1: `XPmanFX` changes** — add imports `com.ogerardin.xpman.settings.SettingsCategory`, `com.ogerardin.xpman.settings.SettingsController`, `javafx.stage.Modality`; make `buildController` `public`; replace `manageTools()` and add `settings()`:

```java
@FXML
public void manageTools() {
    settings(SettingsCategory.TOOLS);
}

@FXML
@SneakyThrows
public void settings() {
    settings(SettingsCategory.GENERAL);
}

@SneakyThrows
public void settings(SettingsCategory category) {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings/settings.fxml"));
    loader.setControllerFactory(this::buildController);
    Pane pane = loader.load();
    loader.<SettingsController>getController().select(category);
    Stage stage = new Stage();
    stage.setTitle("Settings");
    Scene scene = new Scene(pane);
    scene.getStylesheets().add(getClass().getResource("/css/xpman.css").toExternalForm());
    stage.setScene(scene);
    stage.initOwner(primaryStage);
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.showAndWait();
}
```

- [ ] **Step 2: `main.fxml`** — insert after the "Manage tools..." item:

```xml
<MenuItem mnemonicParsing="false" onAction="#settings" text="Settings...">
    <accelerator>
        <KeyCodeCombination alt="UP" code="COMMA" control="UP" meta="UP" shift="UP" shortcut="DOWN" />
    </accelerator>
</MenuItem>
```

- [ ] **Step 3: Remove `Section.TOOLS`** — delete the `TOOLS(...)` line from `Section.java`; update the `installSectionAccelerators` javadoc to "Alt+1..5 and Shortcut+1..5".
- [ ] **Step 4: Compile + tests** — `mvn test -pl xpman-fx` → PASS.
- [ ] **Step 5: Commit** — `feat: add Settings dialog, move tools pane from sidebar to Settings`

---

### Task 5: CSS + full build + manual verification

**Files:**
- Modify: `xpman-fx/src/main/resources/css/xpman.css`

- [ ] **Step 1: CSS** — AtlantaFX `-color-*` variables only (append after the sidebar section):

```css
/* --- Settings dialog --- */
.settings-dialog .settings-category-tree {
    -fx-background-color: -color-bg-subtle;
    -fx-border-color: -color-border-muted;
    -fx-border-width: 0 1 0 0;
}

.settings-dialog .settings-button-bar {
    -fx-padding: 8px 12px;
    -fx-spacing: 8px;
    -fx-border-color: -color-border-muted;
    -fx-border-width: 1 0 0 0;
}

.settings-dialog .settings-pane {
    -fx-padding: 12px;
    -fx-spacing: 8px;
}
```

- [ ] **Step 2: Full build** — `mvn -B -DskipTests clean package` → BUILD SUCCESS.
- [ ] **Step 3: Manual verification** (run `XPmanFX` from IDE with VM option `--add-opens=javafx.graphics/javafx.scene=org.controlsfx.controls`):
  - File > Settings... (and `Shortcut+,`) opens modal dialog; category tree shows General/Tools with icons
  - General: uncheck "Confirm quit" → OK → quit no longer asks; re-enable → asks again; Cancel discards
  - Tools category shows the tools pane (incl. "No tools to show" empty state with no X-Plane folder open); install/uninstall still work
  - File > Manage tools... opens Settings pre-selected on Tools
  - Sidebar no longer has Tools; Alt+1..5 navigate remaining sections
  - Dark + light themes both look right
- [ ] **Step 4: Commit** — `feat: style Settings dialog`
