package com.ogerardin.xpman.settings;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.panels.home.HomeController;
import com.ogerardin.xpman.shell.Section;
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
    private CheckBox ignoreBetaUpdatesCheckBox;

    @FXML
    private void initialize() {
        confirmQuitCheckBox.setSelected(mainController.getConfig().isConfirmQuit());
        ignoreBetaUpdatesCheckBox.setSelected(mainController.getConfig().isIgnoreBetaUpdates());
    }

    @Override
    public void apply() {
        var config = mainController.getConfig();
        boolean wasIgnoringBetas = config.isIgnoreBetaUpdates();
        config.setConfirmQuit(confirmQuitCheckBox.isSelected());
        config.setIgnoreBetaUpdates(ignoreBetaUpdatesCheckBox.isSelected());
        if (config.isIgnoreBetaUpdates() != wasIgnoringBetas) {
            refreshUpdateDisplays();
        }
    }

    /** The preference is read live, but already-rendered update displays need a reload to re-evaluate it. */
    private void refreshUpdateDisplays() {
        XPlane xPlane = mainController.xPlaneProperty().get();
        if (xPlane == null) return;
        // manager reloads fire LOADED, which re-renders every panel's update badges and the Home Skunkcrafts sections
        xPlane.getAircraftManager().reload();
        xPlane.getSceneryManager().reload();
        xPlane.getPluginManager().reload();
        if (mainController.getSectionController(Section.HOME) instanceof HomeController home) {
            home.refreshXPlaneUpdates();
        }
    }
}
