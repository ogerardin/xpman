package com.ogerardin.xpman;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xpman.config.Config;
import com.ogerardin.xpman.config.ConfigManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

@Slf4j
public class XPmanFX extends Application {

    @FXML
    private Menu recentMenu;

    @FXML
    private TabPane tabPane;

    private Stage primaryStage;

    private static final XPlaneInstanceProperty xPlaneInstanceProperty = new XPlaneInstanceProperty();

    private final Config config = ConfigManager.INSTANCE.load();


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        setupStage(primaryStage);
        primaryStage.show();
    }


    @SneakyThrows
    private void setupStage(Stage stage) throws java.io.IOException {
        stage.setTitle("XPman");
        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            quit();
        });

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        loader.setControllerFactory(this::buildController);
        Pane mainPane = loader.load();
        stage.setScene(new Scene(mainPane));
    }

    public static void main(String[] args) {
        launch(args);
    }

    @FXML
    protected void quit() {
        Alert alert = new Alert(CONFIRMATION, "Do you really want to quit?");
        alert.showAndWait()
                .filter(buttonType -> buttonType == ButtonType.OK)
                .ifPresent(buttonType -> {
                    Platform.exit();
                    System.exit(0);
                });
    }

    @FXML
    public void open() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            openXPlane(selectedDirectory);
        }
    }

    @SneakyThrows
    private void openXPlane(File selectedDirectory) {
        Path folder = selectedDirectory.toPath().toRealPath();
        XPlaneInstance xplane = new XPlaneInstance(folder);
        xPlaneInstanceProperty.set(xplane);
        config.setLastXPlanePath(folder.toString());
        config.getRecentPaths().add(folder.toString());
        ConfigManager.INSTANCE.save(config);
        updateRecent();
    }

    @SneakyThrows
    private <C> C buildController(Class<C> type) {
        try {
            // if the controller class has a constuctor that takes a XPlaneInstanceProperty, use it
            Constructor<C> constructor = type.getConstructor(XPlaneInstanceProperty.class);
            return constructor.newInstance(xPlaneInstanceProperty);
        } catch (NoSuchMethodException e) {
            // otherwise use no-arg constructor
            return type.newInstance();
        }
    }

    @SneakyThrows
    public void about() {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/about.fxml"));
        dialog.setDialogPane(loader.load());
        dialog.show();
    }

    @FXML
    private void initialize() {
        updateRecent();
    }

    private void updateRecent() {
        recentMenu.getItems().clear();
        List<MenuItem> menuItems = config.getRecentPaths().stream()
                .map(RecentMenuItem::new)
                .collect(Collectors.toList());
        recentMenu.getItems().addAll(menuItems);
    }

    private class RecentMenuItem extends MenuItem {
        public RecentMenuItem(String folder) {
            super(folder);
            setOnAction(event -> Platform.runLater(() -> openXPlane(new File(folder))));
        }
    }
}