package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.config.install.Installer;
import com.ogerardin.xpman.util.jfx.wizard.Wizard;
import lombok.Getter;

public class InstallWizard extends Wizard {

    @Getter
    private final Installer installer;

    public InstallWizard(Installer installer) {
        super("Install wizard");
        this.installer = installer;
        this.setFlow("/fxml/wizard/page1.fxml",
                "/fxml/wizard/page2.fxml",
                "/fxml/wizard/page3.fxml");
    }
}
