package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.tools.Manifest;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.util.jfx.console.ConsoleController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

public class ToolsController {

    private final XPlane xPlane;

    @FXML
    private TextFlow detail;

    @FXML
    private ToggleButton installedButton;

    @FXML
    private TableView<UiTool> tableView;

    private FilteredList<UiTool> filteredList= new FilteredList<>(FXCollections.emptyObservableList());

    public ToolsController(XPmanFX mainController) {
        this.xPlane = mainController.xPlaneProperty().get();
    }

    @FXML
    public void initialize() {
        xPlane.getToolsManager().registerListener(event -> {
            if (event instanceof ManagerEvent.Loaded<Tool> e) {
                setItems(e.getItems());
            }
        });
        xPlane.getToolsManager().reload();

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> Platform.runLater( () -> displayDetail(newValue))
        );

        // initially display installed
        installedButton.fire();
    }

    private void displayDetail(UiTool uiTool) {
        if (uiTool == null) {
            detail.getChildren().clear();
            return;
        }
        Text title = new Text(uiTool.getName() + "\n");
        Font origFont = title.getFont();
        Font titleFont = Font.font(origFont.getFamily(), origFont.getSize() * 2);
        title.setFont(titleFont);
        Manifest manifest = uiTool.getManifest();
        Text descr;
        if (manifest != null) {
            descr = new Text("\n" + manifest.getDescription());
        }
        else {
            descr = new Text("No description available");
        }
        Hyperlink hyperlink = new Hyperlink("More info");
        detail.getChildren().setAll(title, descr, hyperlink);
    }

    private void setItems(List<Tool> tools) {
        List<UiTool> uiTools = tools.stream()
                .map(tool -> new UiTool(tool, this))
                .toList();
        ObservableList<UiTool> observableUiTools = FXCollections.observableList(uiTools);
        // create new FilteredList with same predicate as previous one (to retain current filter)
        filteredList = new FilteredList<>(observableUiTools, filteredList.getPredicate());
        tableView.setItems(filteredList);
    }

    @FXML
    public void filterInstalled(ActionEvent event) {
        filteredList.setPredicate(event.getSource() instanceof ToggleButton button && !button.isSelected() ? (uiTool -> true) : UiTool::isRunnable);
    }

    @FXML
    public void filterAvailable(ActionEvent event) {
        filteredList.setPredicate(event.getSource() instanceof ToggleButton button && !button.isSelected() ? (uiTool -> true) : UiTool::isInstallable);
    }

    public void reload() {
        xPlane.getToolsManager().reload();
    }

    @SneakyThrows
    public void installTool(Tool tool) {
        ConsoleController consoleController = displayConsole("Installing " + tool.getName());
        Executors.newSingleThreadExecutor().submit(() -> {
            xPlane.getToolsManager().installTool(tool, consoleController);
        });
    }

    @SneakyThrows
    public void uninstallTool(Tool tool) {
        ConsoleController consoleController = displayConsole("Uninstalling " + tool.getName());
        Executors.newSingleThreadExecutor().submit(() -> {
            xPlane.getToolsManager().uninstallTool(tool, consoleController);
        });
    }

    public void runTool(Tool tool) {
        xPlane.getToolsManager().launchTool(tool);
    }

    public static ConsoleController displayConsole(@NonNull String title) throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader loader = new FXMLLoader(ConsoleController.class.getResource("/fxml/console.fxml"));
        dialog.setDialogPane(loader.load());
        dialog.setTitle(title);
        dialog.show();
        return loader.getController();
    }

}
