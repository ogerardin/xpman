package com.ogerardin.xpman.util.jfx.wizard;

import org.controlsfx.dialog.WizardPane;

/** Callback interface for wizard pages to receive notifications when entering or exiting the page. */
public interface PageListener {

    default void onEnteringPage(WizardPane wizardPane) {}

    default void onExitingPage(WizardPane wizardPane) {}

}
