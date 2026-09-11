package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.GenericInstaller;
import com.ogerardin.xpman.util.jfx.wizard.Wizard;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class InstallWizard extends Wizard {

    @Getter(AccessLevel.PACKAGE)
    private final XPlane xPlane;

    @Getter @Setter(AccessLevel.PACKAGE)
    private GenericInstaller installer;

    public InstallWizard(XPlane xPlane) {
        super("Install wizard");
        this.xPlane = xPlane;
        this.setFlow("/fxml/install_wizard/page1.fxml",
                "/fxml/install_wizard/page2.fxml",
                "/fxml/install_wizard/page3.fxml");
    }
}
