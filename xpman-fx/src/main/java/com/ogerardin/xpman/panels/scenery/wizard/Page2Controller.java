package com.ogerardin.xpman.panels.scenery.wizard;

import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.WizardPane;

import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class Page2Controller implements PageListener {

    @NonNull
    private final OrganizeWizard wizard;

    @FXML
    private TableView<SceneryPackage> sceneryTable;

    @Override
    public void onEnteringPage(WizardPane wizardPane) {
        // get the list of enabled scenery packages, ordered by rank in sceneryPackages.ini
        var sceneryPackages = wizard.getXPlane().getSceneryManager().getSceneryPackages().stream()
                .filter(SceneryPackage::isEnabled)
                .sorted(Comparator.comparingInt(SceneryPackage::getRank))
                .collect(Collectors.toList());
        // apply scenery organizer rules to get new ordered list
        var sceneryOrganizer = wizard.getSceneryOrganizer();
        var sortedSceneryPacks = sceneryOrganizer.apply(sceneryPackages);
        // display the result
        sceneryTable.setItems(new ObservableListWrapper<>(sortedSceneryPacks));
    }
}
