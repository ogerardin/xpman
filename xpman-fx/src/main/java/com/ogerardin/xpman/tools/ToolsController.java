package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xpman.XPmanFX;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;

import java.util.List;

public class ToolsController {

    private final XPlane xPlane;

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

        // initially display installed
        installedButton.fire();
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

    public void installTool(Tool tool) {
        xPlane.getToolsManager().installTool(tool);
    }

    public void uninstallTool(Tool tool) {
        xPlane.getToolsManager().uninstallTool(tool);
    }

    public void runTool(Tool tool) {
        xPlane.getToolsManager().launchTool(tool);
    }
}
