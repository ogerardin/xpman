package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xpman.util.panels.TableViewController;
import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.aircrafts.install.AircraftInstaller;
import com.ogerardin.xplane.diag.CheckResult;
import com.ogerardin.xplane.diag.Severity;
import com.ogerardin.xpman.XPlaneInstanceProperty;
import com.ogerardin.xpman.panels.diag.DiagController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AircraftsController extends TableViewController<XPlaneInstance, UiAircraft> {

    private static final Label PLACEHOLDER = new Label("No aircrafts to show");

    private static final Image DISABLED_IMAGE = new Image(AircraftsController.class.getResource("/disabled.png").toExternalForm());

    @FXML
    private TableColumn<UiAircraft, Path> thumbColumn;

    @FXML
    private TableView<UiAircraft> aircraftsTable;

    public AircraftsController(XPlaneInstanceProperty xPlaneInstanceProperty) {
        super(
                xPlaneInstanceProperty,
                xPlaneInstance -> xPlaneInstance.getAircraftManager().getAircrafts().stream()
                        .map(aircraft -> new UiAircraft(aircraft, xPlaneInstance))
                        .collect(Collectors.toList())
        );
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

    @SneakyThrows
    public void installAircraft(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file == null) {
            return;
        }

        installAircraftFromZip(file.toPath());
    }

    @SneakyThrows
    private void installAircraftFromZip(Path zipfile) {
        XPlaneInstance xPlaneInstance = getPropertyValue();

        CheckResult checkResult = AircraftInstaller.checkZip(xPlaneInstance, zipfile);
        Severity severity = checkResult.getSeverity();
        if (severity == Severity.ERROR) {
            Alert alert = new Alert(AlertType.ERROR, checkResult.getMessage());
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert((severity == Severity.WARN) ? AlertType.WARNING : AlertType.CONFIRMATION, checkResult.getMessage());
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        AircraftInstaller.installZip(xPlaneInstance, zipfile);
    }

    @SneakyThrows
    public static void displayCheckResults(List<CheckResult> results) {
        FXMLLoader loader = new FXMLLoader(AircraftsController.class.getResource("/fxml/diag.fxml"));
        Pane mainPane = loader.load();
        DiagController controller = loader.getController();
        controller.setItems(results);
        Stage stage = new Stage();
        stage.setTitle("Analysis results");
        stage.setScene(new Scene(mainPane));
        stage.show();
    }
}
