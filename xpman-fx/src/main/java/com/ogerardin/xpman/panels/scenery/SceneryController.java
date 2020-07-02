package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.javafx.panels.TableViewController;
import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.scenery.SceneryPackage;
import com.ogerardin.xpman.XPlaneInstanceProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import lombok.ToString;

public class SceneryController extends TableViewController<XPlaneInstance, SceneryPackage> {

    @FXML
    @ToString.Exclude
    private TableView<SceneryPackage> sceneryTable;

    public SceneryController(XPlaneInstanceProperty xPlaneInstanceProperty) {
        super(
                xPlaneInstanceProperty,
                xPlaneInstance -> xPlaneInstance.getSceneryManager().getPackages());
    }

    @FXML
    public void initialize() {
        setTableView(sceneryTable);
    }

    public void installScenery() {
    }
}
