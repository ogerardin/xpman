package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.XPlaneInstance;
import com.ogerardin.xplane.install.GenericInstaller;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xpman.util.jfx.wizard.Wizard;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class InstallWizard extends Wizard {

    @Getter(AccessLevel.PACKAGE)
    private final XPlaneInstance xPlaneInstance;

    @Getter(AccessLevel.PACKAGE)
    private final InstallType installType;

    @Getter @Setter(AccessLevel.PACKAGE)
    private GenericInstaller installer;

    public InstallWizard(XPlaneInstance xPlaneInstance) {
        this(xPlaneInstance, null);
    }

    public InstallWizard(XPlaneInstance xPlaneInstance, InstallType installType) {
        super("Install wizard");
        this.xPlaneInstance = xPlaneInstance;
        this.installType = installType;
        this.setFlow("/fxml/wizard/page1.fxml",
                "/fxml/wizard/page2.fxml",
                "/fxml/wizard/page3.fxml");
    }
}
