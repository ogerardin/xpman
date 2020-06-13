package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xpman.XPlaneInstanceProperty;
import com.ogerardin.xpman.panels.DefaultPanelController;
import com.ogerardin.xpman.platform.Platform;
import com.ogerardin.xpman.platform.Platforms;
import com.ogerardin.xpman.platform.UserLabel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Slf4j
public class AircraftsController extends DefaultPanelController<Aircraft> {

    private static final Label PLACEHOLDER = new Label("No aircrafts to show");

    @FXML
    private MenuItem revealMenuIem;

    @FXML
    @ToString.Exclude
    private TableColumn<Aircraft, Path> thumbColumn;

    @FXML
    @ToString.Exclude
    private TableView<Aircraft> aircraftsTable;

    public AircraftsController(XPlaneInstanceProperty xPlaneInstanceProperty) {
        super(
                xPlaneInstanceProperty,
                xPlaneInstance -> xPlaneInstance.getAircraftManager().getAircrafts()
        );
    }

    @FXML
    public void initialize() {
        setTableView(aircraftsTable);
        aircraftsTable.placeholderProperty().setValue(PLACEHOLDER);
        thumbColumn.setCellFactory((TableColumn<Aircraft, Path> col) -> new TableCell<Aircraft, Path>() {
            @Override
            protected void updateItem(Path thumbFile, boolean empty) {
                ImageView imageView = null;
                if (thumbFile != null) {
                    Image image;
                    try (InputStream inputStream = Files.newInputStream(thumbFile)) {
                        image = new Image(inputStream);
                        imageView = new ImageView(image);
//                    imageView.setFitWidth(100);
//                    imageView.setPreserveRatio(true);
//                    imageView.setSmooth(true);
//                    imageView.setCache(true);
                    } catch (IOException e) {
                        log.warn("Failed to load thumbnail: {}", thumbFile);
                    }
                }
                setGraphic(imageView);
            }
        });

        // set label according to annotation @UserLabel Platform.reveal
        Platform platform = Platforms.getCurrent();
        Arrays.stream(platform.getClass().getMethods())
                .filter(method -> method.getName().equals("reveal"))
                .findAny()
                .map(method -> method.getAnnotation(UserLabel.class))
                .ifPresent(userLabel -> revealMenuIem.setText(userLabel.value()));
    }

    public void reveal() throws IOException, InterruptedException {
        Aircraft selectedItem = aircraftsTable.getSelectionModel().getSelectedItem();
        Path file = selectedItem.getAcfFile().getFile();
        Platforms.getCurrent().reveal(file);

    }
}
