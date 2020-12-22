package com.ogerardin.xpman.util.jfx.wizard;

import javafx.beans.property.ReadOnlyBooleanProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

/**
 * Inspired by from https://github.com/controlsfx/controlsfx/issues/604#issuecomment-450456446
 */
@Slf4j
public class ValidatingWizardPane extends WizardPane implements Validating {

    @Setter
    private ReadOnlyBooleanProperty invalidProperty;
    public final ReadOnlyBooleanProperty invalidProperty() {
        return invalidProperty;
    }

    @Getter @Setter
    private FlowListener flowListener;

    // bind/unbind page validation support to wizard's invalidProperty

    @Override
    public void onEnteringPage(Wizard wizard) {
        log.debug("Entering wizard page '{}'", this.getHeaderText());
        ReadOnlyBooleanProperty ip = this.invalidProperty();
        wizard.invalidProperty().unbind();
        if (ip != null) {
            wizard.invalidProperty().bind(ip);
        }
        if (flowListener != null) {
            flowListener.onEnteringPage(wizard);
        }
    }

    @Override
    public void onExitingPage(Wizard wizard) {
        log.debug("Exiting wizard page '{}'", this.getHeaderText());
        log.debug("settings: {}", wizard.getSettings());
        wizard.invalidProperty().unbind();
        if (flowListener != null) {
            flowListener.onExitingPage(wizard);
        }
    }
}
