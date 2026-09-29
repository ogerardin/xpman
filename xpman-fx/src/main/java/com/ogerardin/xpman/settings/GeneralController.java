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
