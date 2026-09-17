package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xpman.util.jfx.wizard.Wizard;
import lombok.Getter;

/**
 * Wizard for performing Skunkcrafts updates.
 * Shows a summary page followed by a progress page.
 */
public class SkunkcraftsUpdateWizard extends Wizard {

    @Getter
    private final SkunkcraftsUpdatable updatable;

    public SkunkcraftsUpdateWizard(SkunkcraftsUpdatable updatable) {
        super("Skunkcrafts Update");
        this.updatable = updatable;
        setFlow(
            "/fxml/install_wizard/skunkcrafts_page1.fxml",
            "/fxml/install_wizard/skunkcrafts_page2.fxml"
        );
    }
}
