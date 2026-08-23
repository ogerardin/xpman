package com.ogerardin.xpman.shell;

import com.ogerardin.xpman.XPmanFX;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Controller of the main window sidebar: the navigation section list, plus a footer with
 * the theme toggle, About action, and app version. Exposes the selected section as a
 * property that {@link XPmanFX} observes to swap the content area.
 */
@RequiredArgsConstructor
public class SidebarController {

    private static final int ICON_SIZE = 16;

    private final XPmanFX xpmanFX;

    private final ObjectProperty<Section> selectedSection = new SimpleObjectProperty<>();

    private final ToggleGroup toggleGroup = new ToggleGroup();

    @FXML
    private VBox navBox;

    @FXML
    private Button themeButton;

    @FXML
    private Label versionLabel;

    @FXML
    private void initialize() {
        for (Section section : Section.values()) {
            FontIcon icon = new FontIcon(section.getIconLiteral());
            icon.setIconSize(ICON_SIZE);
            ToggleButton button = new ToggleButton(section.getLabel());
            button.setGraphic(icon);
            button.getStyleClass().add("sidebar-item");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setToggleGroup(toggleGroup);
            button.setUserData(section);
            button.setOnAction(__ -> setSelectedSection(section));
            navBox.getChildren().add(button);
        }
        toggleGroup.selectedToggleProperty().addListener((__, ___, toggle) -> {
            if (toggle == null && selectedSection.get() != null) {
                // keep a section selected at all times: restore the toggle for the current section
                select(selectedSection.get());
            }
        });

        String version = getClass().getPackage().getImplementationVersion();
        versionLabel.setText(version != null ? "v" + version : "");

        updateThemeButton();
    }

    public ObjectProperty<Section> selectedSectionProperty() {
        return selectedSection;
    }

    public Section getSelectedSection() {
        return selectedSection.get();
    }

    /** Selects the given section and updates the toggle group accordingly. */
    public void select(Section section) {
        setSelectedSection(section);
    }

    private void setSelectedSection(Section section) {
        selectedSection.set(section);
        navBox.getChildren().stream()
                .map(ToggleButton.class::cast)
                .filter(button -> button.getUserData() == section)
                .findFirst()
                .ifPresent(button -> button.setSelected(true));
    }

    @FXML
    private void toggleTheme() {
        xpmanFX.getThemeManager().toggle();
        updateThemeButton();
    }

    @FXML
    private void about() {
        xpmanFX.about();
    }

    private void updateThemeButton() {
        boolean dark = xpmanFX.getThemeManager().isDark();
        themeButton.setText(dark ? "Light theme" : "Dark theme");
        FontIcon icon = new FontIcon(dark ? Feather.SUN : Feather.MOON);
        icon.setIconSize(ICON_SIZE);
        themeButton.setGraphic(icon);
    }
}
