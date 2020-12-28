package com.ogerardin.xpman.util.jfx.wizard;

import javafx.beans.property.ReadOnlyBooleanProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

/**
 * Inspired by  https://github.com/controlsfx/controlsfx/issues/604#issuecomment-450456446
 */
@Slf4j
public class ValidatingWizardPane extends WizardPane implements Validating {

    @Setter
    private ReadOnlyBooleanProperty invalidProperty;
    public final ReadOnlyBooleanProperty invalidProperty() {
        return invalidProperty;
    }

    @Getter @Setter
    private PageListener pageListener;

    @Override
    public void onEnteringPage(Wizard wizard) {
        log.debug("Entering wizard page '{}'", this);
        ReadOnlyBooleanProperty ip = this.invalidProperty();
        wizard.invalidProperty().unbind();
        if (ip != null) {
            wizard.invalidProperty().bind(ip);
        }
        if (pageListener != null) {
            pageListener.onEnteringPage(this);
        }
    }

    @Override
    public void onExitingPage(Wizard wizard) {
        log.debug("Exiting wizard page '{}'", this);
        log.debug("settings: {}", wizard.getSettings());
        wizard.invalidProperty().unbind();
        if (pageListener != null) {
            pageListener.onExitingPage(this);
        }
    }
}
