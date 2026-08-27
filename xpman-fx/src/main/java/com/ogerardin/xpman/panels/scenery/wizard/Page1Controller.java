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
        // No-op: scenery classes are now managed in the Scenery classes window
    }
}
