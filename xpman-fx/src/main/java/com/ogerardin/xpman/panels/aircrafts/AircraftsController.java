package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.XPlaneInstance;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.util.jfx.panels.TableViewController;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Slf4j
public class AircraftsController extends TableViewController<XPlaneInstance, UiAircraft> {

    private static final Label PLACEHOLDER = new Label("No aircrafts to show");

    private static final Image DISABLED_IMAGE = new Image(AircraftsController.class.getResource("/disabled.png").toExternalForm());

    private final ObservableObjectValue<XPlaneInstance> xPlaneInstanceProperty;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableColumn<UiAircraft, Path> thumbColumn;

    @FXML
    private TableView<UiAircraft> aircraftsTable;

    public AircraftsController(XPmanFX mainController) {
        super(
                mainController.xPlaneInstanceProperty(),
                xPlaneInstance -> xPlaneInstance.getAircraftManager().loadAircrafts().stream()
                        .map(aircraft -> new UiAircraft(aircraft, xPlaneInstance))
                        .collect(Collectors.toList())
        );
        xPlaneInstanceProperty = mainController.xPlaneInstanceProperty();
    }

    @FXML
    public void initialize() {
        setTableView(aircraftsTable);

        aircraftsTable.placeholderProperty().setValue(PLACEHOLDER);
        aircraftsTable.setRowFactory(
                tableView -> {
                    final TableRow<UiAircraft> row = new TableRow<>();
                    setContextMenu(row, UiAircraft.class);
                    return row;
                });

        thumbColumn.setCellFactory(AircraftsController::thumbnailCellFactory);

        toolbar.disableProperty().bind(Bindings.isNull(xPlaneInstanceProperty));
    }

    private static TableCell<UiAircraft, Path> thumbnailCellFactory(TableColumn<UiAircraft, Path> col) {
        return new TableCell<UiAircraft, Path>() {
            @Override
            protected void updateItem(Path thumbFile, boolean empty) {
                ImageView thumbnaiImageView = null;
                if (thumbFile != null) {
                    Image image;
                    try (InputStream inputStream = Files.newInputStream(thumbFile)) {
                        image = new Image(inputStream);
                        thumbnaiImageView = new ImageView(image);
//                    imageView.setFitWidth(100);
//                    imageView.setPreserveRatio(true);
//                    imageView.setSmooth(true);
//                    imageView.setCache(true);
                    } catch (IOException e) {
                        log.warn("Failed to load thumbnail: {}", thumbFile);
                    }
                }
                UiAircraft aircraft = (UiAircraft) getTableRow().getItem();
                if (aircraft != null && ! aircraft.isEnabled()) {
                    // aircraft is disabled: add "disabled" icon
                    ImageView disabledImageView = new ImageView(DISABLED_IMAGE);
                    Group group = (thumbnaiImageView != null) ? new Group(thumbnaiImageView, disabledImageView) : new Group(disabledImageView);
                    setGraphic(group);
                }
                else {
                    setGraphic(thumbnaiImageView);
                }
            }
        };
    }

    public void installAircraft() {
        XPlaneInstance xPlaneInstance = getPropertyValue();
        InstallWizard wizard = new InstallWizard(xPlaneInstance, InstallType.AIRCRAFT);
        wizard.showAndWait();
        reload();
    }

}
