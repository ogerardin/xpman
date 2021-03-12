package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.util.jfx.panels.TableViewController;
import com.ogerardin.xpman.util.jfx.panels.menu.IntrospectingContextMenuRowFactory;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AircraftsController extends TableViewController<UiAircraft> {

    private static final Label PLACEHOLDER = new Label("No aircrafts to show");

    private final ObservableObjectValue<XPlane> xPlaneProperty;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableColumn<UiAircraft, Path> thumbColumn;

    @FXML
    private TableView<UiAircraft> aircraftsTable;

    public AircraftsController(XPmanFX mainController) {
        super(
                () -> loadItems(mainController)
        );
        xPlaneProperty = mainController.xPlaneProperty();
        xPlaneProperty.addListener((observable, oldValue, newValue) -> reload());
    }

    private static List<UiAircraft> loadItems(XPmanFX mainController) {
        XPlane xPlane = mainController.xPlaneProperty().get();
        List<Aircraft> aircrafts = xPlane.getAircraftManager().loadAircrafts();
        return aircrafts.stream()
                .map(aircraft -> new UiAircraft(aircraft, xPlane))
                .collect(Collectors.toList());
    }

    @FXML
    public void initialize() {
        setTableView(aircraftsTable);

        aircraftsTable.placeholderProperty().setValue(PLACEHOLDER);
        aircraftsTable.setRowFactory(new IntrospectingContextMenuRowFactory<>(aircraftsTable, UiAircraft.class));

        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));
    }

    public void installAircraft() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.AIRCRAFT);
        wizard.showAndWait();
        reload();
    }

}
