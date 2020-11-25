package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.config.install.Installer;
import com.ogerardin.xpman.util.jfx.wizard.MyWizard;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.file.Path;

public class InstallWizard extends MyWizard {

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
