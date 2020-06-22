package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.config.LinkType;
import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xpman.XPlaneInstanceProperty;
import com.ogerardin.xpman.panels.DefaultPanelController;
import com.ogerardin.xpman.platform.Platform;
import com.ogerardin.xpman.platform.Platforms;
import com.ogerardin.xpman.platform.UserLabel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

@Slf4j
public class AircraftsController extends DefaultPanelController<Aircraft> {

    private static final Label PLACEHOLDER = new Label("No aircrafts to show");

    private static Image DISABLED_IMAGE = new Image(AircraftsController.class.getResource("/disabled.png").toExternalForm());

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

    private static TableCell<Aircraft, Path> thumbnailCellFactory(TableColumn<Aircraft, Path> col) {
        return new TableCell<Aircraft, Path>() {
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
                Aircraft aircraft = (Aircraft) getTableRow().getItem();
                if (aircraft != null && ! aircraft.isEnabled()) {
                    ImageView disabledImageView = new ImageView(DISABLED_IMAGE);
                    Group group = thumbnaiImageView != null ? new Group(thumbnaiImageView, disabledImageView) : new Group(disabledImageView);
                    setGraphic(group);
                }
                else {
                    setGraphic(thumbnaiImageView);
                }
            }
        };
    }

    @FXML
    public void initialize() {
        setTableView(aircraftsTable);
        aircraftsTable.placeholderProperty().setValue(PLACEHOLDER);

        aircraftsTable.setRowFactory(
                tableView -> {
                    final TableRow<Aircraft> row = new TableRow<>();
                    setRowContextMenu(row);
                    return row;
                });

        thumbColumn.setCellFactory(AircraftsController::thumbnailCellFactory);
    }

    private void setRowContextMenu(TableRow<Aircraft> row) {
        // set label according to annotation @UserLabel Platform.reveal
        Platform platform = Platforms.getCurrent();
        String revealLabel = Arrays.stream(platform.getClass().getMethods())
                .filter(method -> method.getName().equals("reveal"))
                .findAny()
                .map(method -> method.getAnnotation(UserLabel.class))
                .map(UserLabel::value)
                .orElse("Show in files");

        // build context menu
        final ContextMenu rowMenu = new ContextMenu();
        Menu linksMenuItem = new Menu("Links");
        MenuItem revealItem = new MenuItem(revealLabel);
        revealItem.setOnAction(event -> reveal(row.getItem()));
        rowMenu.getItems().addAll(linksMenuItem, revealItem);

        // only display context menu for non-null items:
        row.contextMenuProperty().bind(
                Bindings.when(Bindings.isNotNull(row.itemProperty()))
                        .then(rowMenu)
                        .otherwise((ContextMenu) null));

        // cutomize menu for actual row item (links, ...)
        row.setOnContextMenuRequested(event -> {
            Map<LinkType, URL> links = row.getItem().getLinks();
            linksMenuItem.getItems().clear();
            for (Map.Entry<LinkType, URL> entry : links.entrySet()) {
                LinkType linkType = entry.getKey();
                URL url = entry.getValue();
                MenuItem linkMenuItem = new MenuItem(linkType.getLabel());
                linkMenuItem.setOnAction(e -> Platforms.getCurrent().openInBrowser(url));
                linksMenuItem.getItems().add(linkMenuItem);
            }
            linksMenuItem.setVisible(! linksMenuItem.getItems().isEmpty());
        });
    }

    @SneakyThrows
    public void reveal(Aircraft selectedItem) {
        Path file = selectedItem.getAcfFile().getFile();
        Platforms.getCurrent().reveal(file);
    }
}
