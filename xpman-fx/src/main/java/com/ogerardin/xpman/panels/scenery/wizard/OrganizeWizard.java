package com.ogerardin.xpman.panels.scenery.wizard;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import com.ogerardin.xpman.util.jfx.wizard.Wizard;
import lombok.AccessLevel;
import lombok.Getter;

public class OrganizeWizard extends Wizard {

    @Getter(AccessLevel.PACKAGE)
    private final XPlane xPlane;

    @Getter(AccessLevel.PACKAGE)
    private final SceneryOrganizer sceneryOrganizer;

    public OrganizeWizard(XPlane xPlane, SceneryOrganizer sceneryOrganizer) {
        super("Scenary organizer wizard");
        this.xPlane = xPlane;
        this.sceneryOrganizer = sceneryOrganizer;
        this.setFlow("/fxml/organize_wizard/page1.fxml",
                "/fxml/organize_wizard/page2.fxml",
                "/fxml/organize_wizard/page3.fxml");
    }
}
