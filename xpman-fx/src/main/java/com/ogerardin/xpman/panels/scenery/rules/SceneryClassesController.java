package com.ogerardin.xpman.panels.scenery.rules;

import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.panels.scenery.SceneryController;
import com.ogerardin.xpman.scenery_organizer.RegexSceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the standalone Scenery Classes window: hosts the {@link RulesController} editor and persists
 * the edited classes on save.
 */
@RequiredArgsConstructor
public class SceneryClassesController {

    private final XPmanFX xpmanFX;

    @FXML
    private RulesController rulesController;

    @FXML
    private Button closeButton;

    private SceneryController sceneryController;

    @FXML
    public void initialize() {
        SceneryOrganizer organizer = xpmanFX.getSceneryOrganizer();
        rulesController.setItems(new ArrayList<>(organizer.getOrderedSceneryClasses()));
    }

    public void setSceneryController(SceneryController sceneryController) {
        this.sceneryController = sceneryController;
    }

    @FXML
    private void save() {
        List<RegexSceneryClass> classes = rulesController.getItems();
        xpmanFX.getSceneryOrganizer().setOrderedSceneryClasses(classes);
        xpmanFX.getConfig().setSceneryClasses(classes);
        xpmanFX.saveConfig();
        sceneryController.reload();
    }

    @FXML
    private void close() {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
}