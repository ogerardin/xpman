package com.ogerardin.xpman.panels.scenery.wizard;

import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.WizardPane;

import static com.ogerardin.xpman.util.jfx.wizard.Wizard.disableButton;

@Slf4j
public class Page3Controller implements PageListener {

    @FXML
    private Label label;

    @Override
    public void onEnteringPage(WizardPane wizardPane) {
        // disable 'Previous' and 'Finish' buttons
        disableButton(wizardPane, ButtonData.BACK_PREVIOUS, true);
        disableButton(wizardPane, ButtonData.NEXT_FORWARD, true);
    }

}
