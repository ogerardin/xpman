package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.GenericInstaller;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xpman.util.jfx.wizard.Wizard;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class InstallWizard extends Wizard {

    @Getter(AccessLevel.PACKAGE)
    private final XPlane xPlane;

    @Getter(AccessLevel.PACKAGE)
    private final InstallType installType;

    @Getter @Setter(AccessLevel.PACKAGE)
    private GenericInstaller installer;

    public InstallWizard(XPlane xPlane) {
        this(xPlane, null);
    }

    public InstallWizard(XPlane xPlane, InstallType installType) {
        super("Install wizard");
        this.xPlane = xPlane;
        this.installType = installType;
        this.setFlow("/fxml/wizard/page1.fxml",
                "/fxml/wizard/page2.fxml",
                "/fxml/wizard/page3.fxml");
    }
}
