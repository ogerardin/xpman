# Tools Screen Modernization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Modernize the Tools screen to match IntelliJ plugins UI pattern with card-based list and rich detail panel.

**Architecture:** Replace TableView with custom card-based list (ToolCardView), replace TextFlow with structured detail panel (ToolDetailView), extend Manifest model with optional ToolIcon field, refactor ToolsController to extend Controller base class and use ManagerItemsObservableList for consistency with other panels.

**Tech Stack:** Java 25, JavaFX 25, Gson, Lombok, Ikonli Feather icons, AtlantaFX CSS variables

**Spec:** `docs/superpowers/specs/2026-08-25-tools-screen-modernization-design.md`

---

## File Structure

**New files:**
- `xpman-api/src/main/java/com/ogerardin/xplane/tools/ToolIcon.java` — sealed interface for tool icons (URL, Resource, IconFont)
- `xpman-api/src/test/java/com/ogerardin/xplane/tools/ToolIconTest.java` — tests for ToolIcon deserialization
- `xpman-fx/src/main/java/com/ogerardin/xpman/tools/ToolCardView.java` — card component for tool list
- `xpman-fx/src/main/java/com/ogerardin/xpman/tools/ToolDetailView.java` — detail panel component

**Modified files:**
- `xpman-api/src/main/java/com/ogerardin/xplane/tools/Manifest.java` — add optional `icon` field
- `xpman-api/src/main/java/com/ogerardin/xplane/tools/JsonManifestLoader.java` — register ToolIcon Gson adapter
- `xpman-api/src/test/java/com/ogerardin/xplane/tools/JsonManifestLoaderTest.java` — add tests for icon field
- `xpman-fx/src/main/java/com/ogerardin/xpman/tools/ToolsController.java` — extend Controller, use ManagerItemsObservableList, fix toggle
- `xpman-fx/src/main/resources/fxml/tools/tools.fxml` — new layout with card list + detail panel
- `xpman-fx/src/main/resources/css/xpman.css` — add tool card and detail styles

---

### Task 1: Add ToolIcon sealed interface

**Files:**
- Create: `xpman-api/src/main/java/com/ogerardin/xplane/tools/ToolIcon.java`
- Create: `xpman-api/src/test/java/com/ogerardin/xplane/tools/ToolIconTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.ogerardin.xplane.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class ToolIconTest {

    private static Gson gson;

    @BeforeAll
    static void setUp() {
        gson = new GsonBuilder()
                .registerTypeAdapter(ToolIcon.class, (JsonDeserializer<ToolIcon>) (json, __, ___) -> {
                    String value = json.getAsString();
                    if (value.startsWith("http://") || value.startsWith("https://")) {
                        return new ToolIcon.Url(new URL(value));
                    } else if (value.startsWith("/")) {
                        return new ToolIcon.Resource(value);
                    } else {
                        return new ToolIcon.IconFont(value);
                    }
                })
                .create();
    }

    @Test
    void deserializeUrlIcon() {
        String json = "\"https://example.com/icon.png\"";
        ToolIcon icon = gson.fromJson(json, ToolIcon.class);
        assertInstanceOf(ToolIcon.Url.class, icon);
        assertEquals("https://example.com/icon.png", ((ToolIcon.Url) icon).url().toString());
    }

    @Test
    void deserializeResourceIcon() {
        String json = "\"/img/tools/icon.png\"";
        ToolIcon icon = gson.fromJson(json, ToolIcon.class);
        assertInstanceOf(ToolIcon.Resource.class, icon);
        assertEquals("/img/tools/icon.png", ((ToolIcon.Resource) icon).path());
    }

    @Test
    void deserializeIconFont() {
        String json = "\"fth-tool\"";
        ToolIcon icon = gson.fromJson(json, ToolIcon.class);
        assertInstanceOf(ToolIcon.IconFont.class, icon);
        assertEquals("fth-tool", ((ToolIcon.IconFont) icon).literal());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl xpman-api -Dtest=ToolIconTest`
Expected: FAIL with "cannot find symbol: class ToolIcon"

- [ ] **Step 3: Write minimal implementation**

```java
package com.ogerardin.xplane.tools;

import java.net.URL;

/**
 * Represents an icon for a tool. Can be a URL, a classpath resource, or an icon font literal.
 */
public sealed interface ToolIcon permits ToolIcon.Url, ToolIcon.Resource, ToolIcon.IconFont {

    /** An icon loaded from an external URL. */
    record Url(URL url) implements ToolIcon {}

    /** An icon loaded from a classpath resource. */
    record Resource(String path) implements ToolIcon {}

    /** An icon font literal (e.g., "fth-tool" for Feather icons). */
    record IconFont(String literal) implements ToolIcon {}
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -pl xpman-api -Dtest=ToolIconTest`
Expected: PASS (3 tests)

- [ ] **Step 5: Commit**

```bash
git add xpman-api/src/main/java/com/ogerardin/xplane/tools/ToolIcon.java
git add xpman-api/src/test/java/com/ogerardin/xplane/tools/ToolIconTest.java
git commit -m "feat: add ToolIcon sealed interface for tool icons"
```

---

### Task 2: Register Gson adapter for ToolIcon

**Files:**
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/tools/JsonManifestLoader.java:58-66`

- [ ] **Step 1: Add ToolIcon adapter to GSON builder**

Add to the GSON builder chain in `JsonManifestLoader.java`:

```java
.registerTypeAdapter(ToolIcon.class, (JsonDeserializer<ToolIcon>) (json, __, ___) -> {
    String value = json.getAsString();
    if (value.startsWith("http://") || value.startsWith("https://")) {
        return new ToolIcon.Url(new URL(value));
    } else if (value.startsWith("/")) {
        return new ToolIcon.Resource(value);
    } else {
        return new ToolIcon.IconFont(value);
    }
})
```

- [ ] **Step 2: Add import for URL if not already present**

```java
import java.net.URL;
```

- [ ] **Step 3: Run tests to verify nothing broke**

Run: `mvn test -pl xpman-api`
Expected: PASS (all existing tests)

- [ ] **Step 4: Commit**

```bash
git add xpman-api/src/main/java/com/ogerardin/xplane/tools/JsonManifestLoader.java
git commit -m "feat: register Gson adapter for ToolIcon in JsonManifestLoader"
```

---

### Task 3: Add icon field to Manifest record

**Files:**
- Modify: `xpman-api/src/main/java/com/ogerardin/xplane/tools/Manifest.java:33-45`
- Modify: `xpman-api/src/test/java/com/ogerardin/xplane/tools/JsonManifestLoaderTest.java` (add test)

- [ ] **Step 1: Write the failing test**

Add to `JsonManifestLoaderTest.java`:

```java
@Test
void loadManifestWithIcon() throws Exception {
    String json = """
        {
          "name": "Test Tool",
          "icon": "https://example.com/icon.png",
          "description": "Test description",
          "version": "1.0.0",
          "url": "https://example.com/tool.zip",
          "file": "tool.exe"
        }
        """;
    try (InputStream is = new ByteArrayInputStream(json.getBytes())) {
        Manifest manifest = JsonManifestLoader.loadManifest(is, "test.json");
        assertNotNull(manifest.icon());
        assertInstanceOf(ToolIcon.Url.class, manifest.icon());
        assertEquals("https://example.com/icon.png", ((ToolIcon.Url) manifest.icon()).url().toString());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl xpman-api -Dtest=JsonManifestLoaderTest#loadManifestWithIcon`
Expected: FAIL with "cannot find symbol: variable icon"

- [ ] **Step 3: Add icon field to Manifest record**

Add `ToolIcon icon` as the last field in the `Manifest` record.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -pl xpman-api -Dtest=JsonManifestLoaderTest#loadManifestWithIcon`
Expected: PASS

- [ ] **Step 5: Run all tests to verify nothing broke**

Run: `mvn test -pl xpman-api`
Expected: PASS (all tests)

- [ ] **Step 6: Commit**

```bash
git add xpman-api/src/main/java/com/ogerardin/xplane/tools/Manifest.java
git add xpman-api/src/test/java/com/ogerardin/xplane/tools/JsonManifestLoaderTest.java
git commit -m "feat: add optional icon field to Manifest record"
```

---

### Task 4: Create ToolCardView component

**Files:**
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/tools/ToolCardView.java`

- [ ] **Step 1: Create ToolCardView class**

```java
package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.tools.ToolIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A card displaying a single tool: icon, name, description, version badge, and action button.
 */
public class ToolCardView extends HBox {

    private static final int ICON_SIZE = 32;
    private static final int ICON_FONT_SIZE = 20;

    public ToolCardView(UiTool uiTool) {
        getStyleClass().add("tool-card");
        setSpacing(12);
        setPadding(new Insets(12));
        setAlignment(Pos.CENTER_LEFT);

        Node icon = resolveIcon(uiTool);
        VBox content = buildContent(uiTool);
        Button actionButton = buildActionButton(uiTool);

        HBox.setHgrow(content, Priority.ALWAYS);
        getChildren().addAll(icon, content, actionButton);
    }

    private Node resolveIcon(UiTool uiTool) {
        ToolIcon toolIcon = uiTool.getManifest() != null ? uiTool.getManifest().icon() : null;

        if (toolIcon instanceof ToolIcon.Url(var url)) {
            ImageView imageView = new ImageView(new Image(url.toExternalForm(), ICON_SIZE, ICON_SIZE, true, true));
            imageView.setFitWidth(ICON_SIZE);
            imageView.setFitHeight(ICON_SIZE);
            imageView.getStyleClass().add("tool-card-icon");
            return imageView;
        } else if (toolIcon instanceof ToolIcon.Resource(var path)) {
            var resourceUrl = getClass().getResource(path);
            if (resourceUrl != null) {
                ImageView imageView = new ImageView(new Image(resourceUrl.toExternalForm(), ICON_SIZE, ICON_SIZE, true, true));
                imageView.setFitWidth(ICON_SIZE);
                imageView.setFitHeight(ICON_SIZE);
                imageView.getStyleClass().add("tool-card-icon");
                return imageView;
            }
        } else if (toolIcon instanceof ToolIcon.IconFont(var literal)) {
            FontIcon fontIcon = new FontIcon(literal);
            fontIcon.setIconSize(ICON_FONT_SIZE);
            fontIcon.getStyleClass().add("tool-card-icon");
            return fontIcon;
        }

        FontIcon defaultIcon = new FontIcon(Feather.TOOL);
        defaultIcon.setIconSize(ICON_FONT_SIZE);
        defaultIcon.getStyleClass().add("tool-card-icon");
        return defaultIcon;
    }

    private VBox buildContent(UiTool uiTool) {
        Label nameLabel = new Label(uiTool.getName());
        nameLabel.getStyleClass().add("tool-card-name");

        String description = uiTool.getManifest() != null ? uiTool.getManifest().description() : "";
        Label descriptionLabel = new Label(description != null ? description : "");
        descriptionLabel.getStyleClass().add("tool-card-description");
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);

        Label versionLabel = new Label(uiTool.getVersion() != null ? uiTool.getVersion() : "");
        versionLabel.getStyleClass().add("tool-card-version");

        VBox content = new VBox(4, nameLabel, descriptionLabel, versionLabel);
        content.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(content, Priority.ALWAYS);
        return content;
    }

    private Button buildActionButton(UiTool uiTool) {
        Button button = new Button();
        button.getStyleClass().add("tool-card-action");

        if (uiTool.isInstalled()) {
            button.setText("Run");
            button.setGraphic(new FontIcon(Feather.PLAY));
            button.setOnAction(__ -> uiTool.run());
        } else if (uiTool.isInstallable()) {
            button.setText("Install");
            button.setGraphic(new FontIcon(Feather.DOWNLOAD));
            button.setOnAction(__ -> uiTool.install());
        }

        return button;
    }
}
```

- [ ] **Step 2: Compile to verify no errors**

Run: `mvn compile -pl xpman-fx`
Expected: SUCCESS

- [ ] **Step 3: Commit**

```bash
git add xpman-fx/src/main/java/com/ogerardin/xpman/tools/ToolCardView.java
git commit -m "feat: create ToolCardView component for tool list"
```

---

### Task 5: Create ToolDetailView component

**Files:**
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/tools/ToolDetailView.java`

- [ ] **Step 1: Create ToolDetailView class**

```java
package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.tools.InstalledTool;
import com.ogerardin.xplane.tools.Manifest;
import com.ogerardin.xplane.tools.ToolIcon;
import com.ogerardin.xplane.util.platform.Platforms;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A detail panel displaying comprehensive information about a selected tool.
 */
public class ToolDetailView extends VBox {

    private static final int LARGE_ICON_SIZE = 64;
    private static final int LARGE_ICON_FONT_SIZE = 40;

    public ToolDetailView() {
        getStyleClass().add("tool-detail");
        setSpacing(16);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_LEFT);
    }

    public void setTool(UiTool uiTool) {
        getChildren().clear();
        if (uiTool == null) {
            return;
        }
        getChildren().addAll(
                buildHeader(uiTool),
                buildDescription(uiTool),
                buildMetadata(uiTool),
                buildHomepage(uiTool)
        );
    }

    private Node buildHeader(UiTool uiTool) {
        Node icon = resolveLargeIcon(uiTool);

        Label nameLabel = new Label(uiTool.getName());
        nameLabel.getStyleClass().add("tool-detail-name");

        Label versionLabel = new Label(uiTool.getVersion() != null ? uiTool.getVersion() : "");
        versionLabel.getStyleClass().add("tool-detail-version");

        Label statusLabel = new Label(uiTool.isInstalled() ? "Installed" : "Available");
        statusLabel.getStyleClass().add("tool-detail-status");

        VBox textContent = new VBox(4, nameLabel, versionLabel, statusLabel);
        HBox header = new HBox(16, icon, textContent);
        header.getStyleClass().add("tool-detail-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private Node resolveLargeIcon(UiTool uiTool) {
        ToolIcon toolIcon = uiTool.getManifest() != null ? uiTool.getManifest().icon() : null;

        if (toolIcon instanceof ToolIcon.Url(var url)) {
            ImageView imageView = new ImageView(new Image(url.toExternalForm(), LARGE_ICON_SIZE, LARGE_ICON_SIZE, true, true));
            imageView.setFitWidth(LARGE_ICON_SIZE);
            imageView.setFitHeight(LARGE_ICON_SIZE);
            imageView.getStyleClass().add("tool-detail-icon");
            return imageView;
        } else if (toolIcon instanceof ToolIcon.Resource(var path)) {
            var resourceUrl = getClass().getResource(path);
            if (resourceUrl != null) {
                ImageView imageView = new ImageView(new Image(resourceUrl.toExternalForm(), LARGE_ICON_SIZE, LARGE_ICON_SIZE, true, true));
                imageView.setFitWidth(LARGE_ICON_SIZE);
                imageView.setFitHeight(LARGE_ICON_SIZE);
                imageView.getStyleClass().add("tool-detail-icon");
                return imageView;
            }
        } else if (toolIcon instanceof ToolIcon.IconFont(var literal)) {
            FontIcon fontIcon = new FontIcon(literal);
            fontIcon.setIconSize(LARGE_ICON_FONT_SIZE);
            fontIcon.getStyleClass().add("tool-detail-icon");
            return fontIcon;
        }

        FontIcon defaultIcon = new FontIcon(Feather.TOOL);
        defaultIcon.setIconSize(LARGE_ICON_FONT_SIZE);
        defaultIcon.getStyleClass().add("tool-detail-icon");
        return defaultIcon;
    }

    private Node buildDescription(UiTool uiTool) {
        Manifest manifest = uiTool.getManifest();
        String description = manifest != null ? manifest.description() : null;
        Label label = new Label(description != null ? description : "No description available");
        label.getStyleClass().add("tool-detail-description");
        label.setWrapText(true);
        return label;
    }

    private Node buildMetadata(UiTool uiTool) {
        Manifest manifest = uiTool.getManifest();
        if (manifest == null) {
            return new Label();
        }

        VBox metadata = new VBox(8);
        metadata.getStyleClass().add("tool-detail-metadata");

        if (manifest.platform() != null) {
            metadata.getChildren().add(new Label("Platform: " + manifest.platform()));
        }
        if (manifest.xplaneVersion() != null) {
            metadata.getChildren().add(new Label("X-Plane version: " + manifest.xplaneVersion()));
        }
        if (uiTool.isInstalled() && uiTool.getTool() instanceof InstalledTool installedTool) {
            metadata.getChildren().add(new Label("Installed at: " + installedTool.getApp()));
        }

        return metadata;
    }

    private Node buildHomepage(UiTool uiTool) {
        Manifest manifest = uiTool.getManifest();
        if (manifest == null || manifest.homepage() == null) {
            return new Label();
        }
        Hyperlink hyperlink = new Hyperlink("Tool homepage");
        hyperlink.getStyleClass().add("tool-detail-homepage");
        hyperlink.setOnAction(__ -> Platforms.getCurrent().openUrl(manifest.homepage()));
        return hyperlink;
    }
}
```

- [ ] **Step 2: Compile to verify no errors**

Run: `mvn compile -pl xpman-fx`
Expected: SUCCESS

- [ ] **Step 3: Commit**

```bash
git add xpman-fx/src/main/java/com/ogerardin/xpman/tools/ToolDetailView.java
git commit -m "feat: create ToolDetailView component for detail panel"
```

---

### Task 6: Refactor ToolsController

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/tools/ToolsController.java`

- [ ] **Step 1: Rewrite ToolsController**

```java
package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.panels.Controller;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.util.jfx.EmptyState;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ToolsController extends Controller {

    private final XPlaneProperty xPlaneProperty;

    @FXML
    private ToggleButton installedButton;

    @FXML
    private ToggleButton availableButton;

    @FXML
    private VBox cardListContainer;

    @FXML
    private ToolDetailView detailView;

    @FXML
    private StackPane placeholderPane;

    private ManagerItemsObservableList<Tool, UiTool> uiItems;
    private FilteredList<UiTool> filteredList;

    public ToolsController(XPmanFX mainController) {
        this.xPlaneProperty = mainController.xPlaneProperty();
    }

    @FXML
    public void initialize() {
        uiItems = new ManagerItemsObservableList<>(
                xPlaneProperty,
                XPlane::getToolsManager,
                tool -> new UiTool(tool, xPlaneProperty.get())
        );

        filteredList = new FilteredList<>(uiItems);
        filteredList.addListener((ListChangeListener<UiTool>) __ -> updateCardList());

        installedButton.setSelected(true);
        filterInstalled();
    }

    private void updateCardList() {
        cardListContainer.getChildren().clear();
        for (UiTool uiTool : filteredList) {
            ToolCardView card = new ToolCardView(uiTool);
            card.setOnMouseClicked(__ -> detailView.setTool(uiTool));
            cardListContainer.getChildren().add(card);
        }

        boolean empty = filteredList.isEmpty();
        placeholderPane.setVisible(empty);
        placeholderPane.setManaged(empty);
        if (empty) {
            placeholderPane.getChildren().setAll(new EmptyState("fth-tool", "No tools to show"));
        }

        if (!filteredList.isEmpty()) {
            detailView.setTool(filteredList.get(0));
        }
    }

    @FXML
    public void filterInstalled() {
        filteredList.setPredicate(UiTool::isInstalled);
    }

    @FXML
    public void filterAvailable() {
        filteredList.setPredicate(UiTool::isInstallable);
    }

    public void reload() {
        uiItems.reload();
    }
}
```

- [ ] **Step 2: Compile to verify no errors**

Run: `mvn compile -pl xpman-fx`
Expected: SUCCESS

- [ ] **Step 3: Commit**

```bash
git add xpman-fx/src/main/java/com/ogerardin/xpman/tools/ToolsController.java
git commit -m "refactor: ToolsController extends Controller and uses ManagerItemsObservableList"
```

---

### Task 7: Rewrite tools.fxml

**Files:**
- Modify: `xpman-fx/src/main/resources/fxml/tools/tools.fxml`

- [ ] **Step 1: Replace tools.fxml with new layout**

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import org.kordamp.ikonli.javafx.FontIcon?>
<BorderPane xmlns="http://javafx.com/javafx/17.0.2-ea"
            xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="com.ogerardin.xpman.tools.ToolsController">
    <top>
        <ToolBar BorderPane.alignment="CENTER">
            <Button mnemonicParsing="false" onAction="#reload" text="Reload">
                <graphic><FontIcon iconLiteral="fth-refresh-cw"/></graphic>
            </Button>
            <Separator/>
            <ToggleButton fx:id="installedButton" onAction="#filterInstalled" text="Installed"/>
            <ToggleButton fx:id="availableButton" onAction="#filterAvailable" text="Available"/>
        </ToolBar>
    </top>
    <center>
        <SplitPane dividerPositions="0.35">
            <StackPane>
                <ScrollPane fitToWidth="true">
                    <VBox fx:id="cardListContainer" styleClass="tool-card-list"/>
                </ScrollPane>
                <StackPane fx:id="placeholderPane" styleClass="empty-state" visible="false" managed="false"/>
            </StackPane>
            <ScrollPane fitToWidth="true">
                <VBox>
                    <fx:define>
                        <com.ogerardin.xpman.tools.ToolDetailView fx:id="detailView"/>
                    </fx:define>
                </VBox>
            </ScrollPane>
        </SplitPane>
    </center>
</BorderPane>
```

- [ ] **Step 2: Compile to verify FXML is valid**

Run: `mvn compile -pl xpman-fx`
Expected: SUCCESS

- [ ] **Step 3: Commit**

```bash
git add xpman-fx/src/main/resources/fxml/tools/tools.fxml
git commit -m "feat: rewrite tools.fxml with card list and detail panel layout"
```

---

### Task 8: Add CSS styles for tool cards and detail panel

**Files:**
- Modify: `xpman-fx/src/main/resources/css/xpman.css`

- [ ] **Step 1: Append tool card and detail styles to xpman.css**

```css
/* --- Tool card list --- */
.tool-card-list {
    -fx-spacing: 0;
    -fx-padding: 0;
}

.tool-card {
    -fx-background-color: -color-bg-subtle;
    -fx-border-color: -color-border-muted;
    -fx-border-width: 0 0 1 0;
    -fx-padding: 12px;
    -fx-spacing: 12px;
    -fx-alignment: center-left;
}

.tool-card:hover {
    -fx-background-color: -color-accent-subtle;
}

.tool-card-icon {
    -fx-icon-color: -color-fg-muted;
    -fx-fill: -color-fg-muted;
}

.tool-card-name {
    -fx-font-weight: bold;
    -fx-font-size: 1.1em;
}

.tool-card-description {
    -fx-text-fill: -color-fg-muted;
    -fx-font-size: 0.95em;
    -fx-max-width: 400px;
}

.tool-card-version {
    -fx-background-color: -color-accent-subtle;
    -fx-text-fill: -color-fg-muted;
    -fx-padding: 2px 8px;
    -fx-background-radius: 4px;
    -fx-font-size: 0.85em;
}

.tool-card-action {
    -fx-background-color: -color-accent-emphasis;
    -fx-text-fill: -color-fg-emphasis;
    -fx-padding: 6px 12px;
    -fx-background-radius: 6px;
}

.tool-card-action:hover {
    -fx-opacity: 0.9;
}

/* --- Tool detail panel --- */
.tool-detail {
    -fx-padding: 20px;
    -fx-spacing: 16px;
}

.tool-detail-header {
    -fx-spacing: 16px;
    -fx-alignment: center-left;
}

.tool-detail-icon {
    -fx-icon-color: -color-fg-muted;
    -fx-fill: -color-fg-muted;
}

.tool-detail-name {
    -fx-font-size: 1.6em;
    -fx-font-weight: bold;
}

.tool-detail-version {
    -fx-text-fill: -color-fg-muted;
    -fx-font-size: 1.1em;
}

.tool-detail-status {
    -fx-background-color: -color-success-subtle;
    -fx-text-fill: -color-success-fg;
    -fx-padding: 4px 10px;
    -fx-background-radius: 6px;
    -fx-font-size: 0.9em;
}

.tool-detail-description {
    -fx-text-fill: -color-fg-default;
    -fx-font-size: 1em;
    -fx-wrap-text: true;
}

.tool-detail-metadata {
    -fx-spacing: 8px;
    -fx-padding: 12px;
    -fx-background-color: -color-bg-subtle;
    -fx-background-radius: 6px;
}

.tool-detail-metadata .label {
    -fx-text-fill: -color-fg-muted;
}

.tool-detail-homepage {
    -fx-text-fill: -color-accent-fg;
    -fx-font-size: 1em;
}

.tool-detail-homepage:hover {
    -fx-underline: true;
}
```

- [ ] **Step 2: Compile to verify CSS is valid**

Run: `mvn compile -pl xpman-fx`
Expected: SUCCESS

- [ ] **Step 3: Commit**

```bash
git add xpman-fx/src/main/resources/css/xpman.css
git commit -m "feat: add CSS styles for tool cards and detail panel"
```

---

### Task 9: Add example icon to tool manifest

**Files:**
- Modify: `xpman-api/src/main/resources/tools/x-updater.json`

- [ ] **Step 1: Add icon field to x-updater.json**

Add `"icon": "fth-download"` to the top-level JSON object.

- [ ] **Step 2: Run tests**

Run: `mvn test -pl xpman-api`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add xpman-api/src/main/resources/tools/x-updater.json
git commit -m "feat: add example icon to x-updater.json manifest"
```

---

### Task 10: Final testing and cleanup

- [ ] **Step 1: Run all tests**

Run: `mvn test`
Expected: PASS (all tests)

- [ ] **Step 2: Build the application**

Run: `mvn -B -DskipTests clean package`
Expected: SUCCESS

- [ ] **Step 3: Manual smoke test**

- Open the application
- Navigate to Tools section
- Verify card list displays correctly
- Verify detail panel updates on selection
- Verify toggle filters work
- Verify actions work
- Test with both dark and light themes

- [ ] **Step 4: Final commit**

```bash
git add .
git commit -m "feat: tools screen modernization complete"
```
