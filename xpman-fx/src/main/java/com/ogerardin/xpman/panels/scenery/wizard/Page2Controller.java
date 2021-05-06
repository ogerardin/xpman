package com.ogerardin.xpman.panels.scenery.wizard;

import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.WizardPane;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class Page2Controller implements PageListener {

    @NonNull
    private final OrganizeWizard wizard;

    @FXML
    private TableView<SceneryPackage> sceneryTable;

    @Override
    public void onEnteringPage(WizardPane wizardPane) {
        // apply scenery organizer rules to X-Plane scenery packs
        final List<SceneryPackage> sceneryPackages = wizard.getXPlane().getSceneryManager().getSceneryPackages();
        final SceneryOrganizer sceneryOrganizer = wizard.getSceneryOrganizer();
        final List<SceneryPackage> sortedSceneryPacks = sceneryOrganizer.apply(sceneryPackages);
        // display the result
        sceneryTable.setItems(new ObservableListWrapper<>(sortedSceneryPacks));
    }
}
