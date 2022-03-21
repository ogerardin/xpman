package com.ogerardin.xpman.tools;

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
    private TableView<UiTool> tableView;

    private FilteredList<UiTool> filteredList;

    public ToolsController(XPmanFX mainController) {
        this.xPlane = mainController.xPlaneProperty().get();
    }

    @FXML
    public void initialize() {
        List<Tool> tools = xPlane.getToolsManager().getTools();
        List<UiTool> uiTools = tools.stream().map(UiTool::new).toList();
        ObservableList<UiTool> observableUiTools = FXCollections.observableList(uiTools);
        filteredList = new FilteredList<>(observableUiTools);
        tableView.setItems(filteredList);
    }

    @FXML
    public void filterInstalled(ActionEvent event) {
        if (event.getSource() instanceof ToggleButton button && ! button.isSelected()) {
            filteredList.setPredicate(uiTool -> true);
        }
        else {
            filteredList.setPredicate(UiTool::isRunnable);
        }
    }

    @FXML
    public void filterAvailable(ActionEvent event) {
        if (event.getSource() instanceof ToggleButton button && ! button.isSelected()) {
            filteredList.setPredicate(uiTool -> true);
        }
        else {
            filteredList.setPredicate(UiTool::isInstallable);
        }
    }
}
