package com.ogerardin.xpman.util.jfx.wizard;

import org.controlsfx.dialog.WizardPane;

public interface PageListener {

    default void onEnteringPage(WizardPane wizardPane) {}

    default void onExitingPage(WizardPane wizardPane) {}

}
