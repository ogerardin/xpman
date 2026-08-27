# Settings Dialog Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Preferences/Settings dialog with tabbed UI (General tab for theme + recent paths, Scenery Classes tab for rules editor), move scenery class editing out of the Organize Wizard, and clean up the theme menu.

**Architecture:** Modal dialog (`DialogPane` + `TabPane`) opened from `File > Preferences...`. A shared `SceneryOrganizer` on `XPmanFX` is the single source of truth for scenery class configuration. The settings dialog and organize wizard both read/write through this shared instance.

**Tech Stack:** JavaFX, FXML, ControlsFX DialogPane, Lombok, existing `rules.fxml` component reuse.

---

## File Map

| Action | File | Purpose |
|--------|------|---------|
| **Create** | `xpman-fx/src/main/resources/fxml/settings.fxml` | Settings dialog FXML |
| **Create** | `xpman-fx/src/main/java/com/ogerardin/xpman/panels/settings/SettingsController.java` | Settings controller |
| **Modify** | `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java` | Add `openSettings()`, own `SceneryOrganizer`, remove theme menu code |
| **Modify** | `xpman-fx/src/main/resources/fxml/main.fxml` | Wire `#openSettings`, remove View menu |
| **Modify** | `xpman-fx/src/main/resources/fxml/organize_wizard/page1.fxml` | Replace rules editor with instructions |
| **Modify** | `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/wizard/Page1Controller.java` | Remove rules logic |
| **Modify** | `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/SceneryController.java` | Receive `SceneryOrganizer` from `XPmanFX` |
| **Modify** | `xpman-fx/src/main/java/module-info.java` | Add `opens panels.settings` |

---

### Task 1: Create SettingsController

**Files:**
- Create: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/settings/SettingsController.java`

- [ ] **Step 1: Create the SettingsController class**

```java
package com.ogerardin.xpman.panels.settings;

import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.panels.scenery.rules.RulesController;
import com.ogerardin.xpman.scenery_organizer.RegexSceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SettingsController {

    private final XPmanFX xpmanFX;

    @FXML
    private RulesController rulesController;

    @FXML
    private ListView<String> recentPathsList;

    @FXML
    private Button removeRecentButton;

    @FXML
    private Button themeButton;

    @FXML
    public void initialize() {
        xpmanFX.getThemeManager().darkProperty().addListener((__, ___, dark) ->
                themeButton.setText(dark ? "Light theme" : "Dark theme"));
        themeButton.setText(xpmanFX.getThemeManager().isDark() ? "Light theme" : "Dark theme");

        recentPathsList.getItems().setAll(xpmanFX.getConfig().getRecentPaths());
        removeRecentButton.disableProperty().bind(
                recentPathsList.getSelectionModel().selectedItemProperty().isNull());

        SceneryOrganizer organizer = xpmanFX.getSceneryOrganizer();
        rulesController.setItems(new ArrayList<>(organizer.getOrderedSceneryClasses()));
    }

    @FXML
    private void toggleTheme() {
        xpmanFX.getThemeManager().toggle();
    }

    @FXML
    private void removeRecentPath() {
        String selected = recentPathsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            recentPathsList.getItems().remove(selected);
        }
    }

    public void save() {
        xpmanFX.getConfig().getRecentPaths().clear();
        xpmanFX.getConfig().getRecentPaths().addAll(recentPathsList.getItems());

        List<RegexSceneryClass> classes = rulesController.getItems();
        xpmanFX.getSceneryOrganizer().setOrderedSceneryClasses(classes);
        xpmanFX.getConfig().setSceneryClasses(classes);

        xpmanFX.saveConfig();
    }
}
```

---

### Task 2: Create settings.fxml

**Files:**
- Create: `xpman-fx/src/main/resources/fxml/settings.fxml`

- [ ] **Step 1: Create the FXML**

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<DialogPane prefWidth="600.0" prefHeight="450.0"
            xmlns="http://javafx.com/javafx/17.0.2-ea"
            xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="com.ogerardin.xpman.panels.settings.SettingsController">
    <ButtonType fx:constant="OK" />
    <ButtonType fx:constant="CANCEL" />
    <content>
        <TabPane>
            <Tab text="General" closable="false">
                <content>
                    <VBox spacing="12.0" style="-fx-padding: 16;">
                        <Label styleClass="label-header" text="Theme"/>
                        <HBox spacing="8.0" alignment="CENTER_LEFT">
                            <Button fx:id="themeButton" onAction="#toggleTheme" text="Dark theme"/>
                        </HBox>
                        <Label styleClass="label-header" text="Recent X-Plane Paths"/>
                        <HBox spacing="8.0" VBox.vgrow="ALWAYS">
                            <ListView fx:id="recentPathsList" prefHeight="200.0" HBox.hgrow="ALWAYS"/>
                            <VBox spacing="4.0" alignment="TOP_CENTER">
                                <Button fx:id="removeRecentButton" onAction="#removeRecentPath" text="Remove"/>
                            </VBox>
                        </HBox>
                    </VBox>
                </content>
            </Tab>
            <Tab text="Scenery Classes" closable="false">
                <content>
                    <fx:include fx:id="rules" source="../rules.fxml"/>
                </content>
            </Tab>
        </TabPane>
    </content>
</DialogPane>
```

---

### Task 3: Wire settings dialog in XPmanFX

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/XPmanFX.java`

- [ ] **Step 1: Add `openSettings()` method and `sceneryOrganizer` field**

Add field:
```java
@Getter
private final SceneryOrganizer sceneryOrganizer = new SceneryOrganizer(getConfig().getSceneryClasses());
```

Add imports:
```java
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import com.ogerardin.xpman.panels.settings.SettingsController;
import javafx.scene.control.ButtonType;
```

Add method:
```java
@FXML
@SneakyThrows
private void openSettings() {
    Dialog<ButtonType> dialog = new Dialog<>();
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
    loader.setControllerFactory(type -> {
        if (type == SettingsController.class) {
            return new SettingsController(this);
        }
        try {
            return type.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });
    dialog.setDialogPane(loader.load());
    dialog.initOwner(primaryStage);
    dialog.setTitle("Preferences");
    dialog.showAndWait().ifPresent(buttonType -> {
        if (buttonType == ButtonType.OK) {
            SettingsController controller = loader.getController();
            controller.save();
            showSection(Section.SCENERY);
        }
    });
}
```

- [ ] **Step 2: Remove theme menu code**

Remove from `XPmanFX.java`:
- The `@FXML private MenuItem themeMenuItem;` field
- The `toggleTheme()` method
- The `updateThemeMenuItem()` method
- The listener on `darkProperty` in `initialize()` (the line `getThemeManager().darkProperty().addListener(...)`)
- The call to `updateThemeMenuItem()` in `initialize()`

---

### Task 4: Update main.fxml

**Files:**
- Modify: `xpman-fx/src/main/resources/fxml/main.fxml`

- [ ] **Step 1: Wire Preferences menu item and remove View menu**

1. Change the Preferences menu item from:
```xml
<MenuItem mnemonicParsing="false" text="Preferences..." />
```
to:
```xml
<MenuItem mnemonicParsing="false" onAction="#openSettings" text="Preferences..." />
```

2. Remove the entire View menu:
```xml
<Menu mnemonicParsing="false" text="View">
    <MenuItem fx:id="themeMenuItem" mnemonicParsing="false" onAction="#toggleTheme" text="Switch to Light Theme" />
</Menu>
```

- [ ] **Step 2: Verify compilation**

Run: `mvn -B -DskipTests compile -pl xpman-fx`
Expected: Compiles successfully

---

### Task 5: Move SceneryOrganizer ownership to XPmanFX

**Files:**
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/SceneryController.java`

- [ ] **Step 1: Update SceneryController constructor**

Change from:
```java
public SceneryController(XPmanFX mainController) {
    xPlaneProperty = mainController.xPlaneProperty();
    sceneryOrganizer = loadSceneryOrganizer(mainController.getConfigManager().getConfig());
}
```
to:
```java
public SceneryController(XPmanFX mainController) {
    xPlaneProperty = mainController.xPlaneProperty();
    sceneryOrganizer = mainController.getSceneryOrganizer();
}
```

- [ ] **Step 2: Remove `loadSceneryOrganizer` method**

Delete the `loadSceneryOrganizer` method entirely.

- [ ] **Step 3: Remove unused import**

Remove `import com.ogerardin.xpman.config.XPManPrefs;` if no longer used.

---

### Task 6: Update Organize Wizard page 1

**Files:**
- Modify: `xpman-fx/src/main/resources/fxml/organize_wizard/page1.fxml`
- Modify: `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/wizard/Page1Controller.java`

- [ ] **Step 1: Replace page1.fxml with instructions**

Replace entire content with:
```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import com.ogerardin.xpman.util.jfx.wizard.ValidatingWizardPane?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.VBox?>
<ValidatingWizardPane prefHeight="400.0" prefWidth="600.0"
                      xmlns="http://javafx.com/javafx/8.0.171"
                      xmlns:fx="http://javafx.com/fxml/1"
                      fx:controller="com.ogerardin.xpman.panels.scenery.wizard.Page1Controller">
    <headerText>This wizard will re-order scenery packages in the scenery_packs.ini file
        according to your configured scenery classes.</headerText>
    <content>
        <VBox spacing="12.0" style="-fx-padding: 16;">
            <Label wrapText="true" text="Scenery packages will be grouped by class and ordered according to your configured rules. Libraries are automatically recognized and don't need to be defined."/>
            <Label wrapText="true" text="To edit scenery classes (add, remove, reorder, or change regex patterns), go to File &gt; Preferences... and select the Scenery Classes tab."/>
            <Label wrapText="true" style="-fx-font-style: italic;" text="Press Next to preview the resulting scenery_packs.ini order."/>
        </VBox>
    </content>
</ValidatingWizardPane>
```

- [ ] **Step 2: Simplify Page1Controller**

Replace entire content with:
```java
package com.ogerardin.xpman.panels.scenery.wizard;

import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.controlsfx.dialog.WizardPane;

@RequiredArgsConstructor
public class Page1Controller implements PageListener {

    @NonNull
    private final OrganizeWizard wizard;

    @Override
    public void onEnteringPage(WizardPane wizardPane) {
        // No-op: scenery classes are now managed in Settings
    }
}
```

---

### Task 7: Update module-info.java

**Files:**
- Modify: `xpman-fx/src/main/java/module-info.java`

- [ ] **Step 1: Add opens for settings package**

Add after `opens com.ogerardin.xpman.panels.about`:
```java
opens com.ogerardin.xpman.panels.settings to javafx.base, javafx.fxml;
```

- [ ] **Step 2: Full build verification**

Run: `mvn -B -DskipTests clean package`
Expected: Full build succeeds

---

### Task 8: Test and verify

- [ ] **Step 1: Run tests**

Run: `mvn test`
Expected: All existing tests pass

- [ ] **Step 2: Manual verification checklist**

- File > Preferences... opens the settings dialog
- General tab: theme toggle works and updates sidebar button
- General tab: recent paths list shows entries, remove button works
- Scenery Classes tab: rules editor shows current classes with add/delete/reorder/restore defaults
- OK saves changes, Cancel discards
- After OK, Scenery panel reflects updated class assignments
- Organize Wizard: page 1 shows instructions, page 2 shows preview, page 3 shows completion
- View menu is gone
- Sidebar theme toggle still works

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: add Settings dialog with General and Scenery Classes tabs"
```
