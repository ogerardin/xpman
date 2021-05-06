package com.ogerardin.xpman.panels.scenery.wizard;

import com.ogerardin.xpman.panels.scenery.rules.RulesController;
import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import javafx.fxml.FXML;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.controlsfx.dialog.WizardPane;

import java.util.List;

@RequiredArgsConstructor
public class Page1Controller implements PageListener {

    @NonNull
    private final OrganizeWizard wizard;

    @FXML
    private RulesController rulesController;

    @FXML
    public void initialize() {
        // display scenery classes in embedded rules editor
        final List<SceneryClass> sceneryClasses = wizard.getSceneryOrganizer().getOrderedSceneryClasses();
        rulesController.setItems(sceneryClasses);
    }

    @Override
    public void onExitingPage(WizardPane wizardPane) {
        // when leaving the page, retrieve edited rules and assign them to the scenery organizer
        final List<SceneryClass> sceneryClasses = rulesController.getItems();
        wizard.getSceneryOrganizer().setOrderedSceneryClasses(sceneryClasses);
    }
}
