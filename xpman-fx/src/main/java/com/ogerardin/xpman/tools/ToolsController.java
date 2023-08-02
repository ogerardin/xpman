package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.tools.Manifest;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xplane.tools.ToolsManager;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.util.jfx.console.ConsoleController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ToolsController {

    private final XPlane xPlane;

    //TODO use a WebView instead of a TextFlow
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
        ToolsManager toolsManager = xPlane.getToolsManager();
        toolsManager.registerListener(event -> {
            if (event.getType() == ManagerEvent.Type.LOADED) {
                setItems(event.getItems());
            }
        });
        toolsManager.reload();

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> Platform.runLater( () -> displayDetail(newValue))
        );

        // initially display installed tools only (simulate click on "Installed" button)
        installedButton.fire();
    }

    private void displayDetail(UiTool uiTool) {
        if (uiTool == null) {
            detail.getChildren().clear();
            return;
        }
        List<Node> nodes = new ArrayList<>();
        {
            Text title = new Text(uiTool.getName() + "\n");
            Font origFont = title.getFont();
            Font titleFont = Font.font(origFont.getFamily(), origFont.getSize() * 2);
            title.setFont(titleFont);
            nodes.add(title);
        }
        Manifest manifest = uiTool.getManifest();
        if (manifest != null) {
            nodes.add(new Text("\n" + manifest.description()));
            if (manifest.homepage() != null) {
                Hyperlink hyperlink = new Hyperlink("Tool homepage");
                hyperlink.setOnAction(event -> Platforms.getCurrent().openUrl(manifest.homepage()) );
                nodes.add(hyperlink);
            }
        } else {
            nodes.add(new Text("No description available"));
        }
        detail.getChildren().setAll(nodes);
    }

    private void setItems(List<Tool> tools) {
        List<UiTool> uiTools = tools.stream()
                .map(tool -> new UiTool(tool, this))
                .toList();
        ObservableList<UiTool> observableUiTools = FXCollections.observableList(uiTools);
        // create new FilteredList with same predicate as previous one (to retain current filter)
        filteredList = new FilteredList<>(observableUiTools, filteredList.getPredicate());
        tableView.setItems(filteredList);
        if (!filteredList.isEmpty()) {
            tableView.getSelectionModel().select(0);
        }
    }

    @FXML
    public void filterInstalled(ActionEvent event) {
        boolean selected = event.getSource() instanceof ToggleButton button && button.isSelected();
        filteredList.setPredicate(selected ? UiTool::isInstalled : (uiTool -> true));
    }

    @FXML
    public void filterAvailable(ActionEvent event) {
        boolean selected = event.getSource() instanceof ToggleButton button && button.isSelected();
        filteredList.setPredicate(selected ? UiTool::isInstallable : (uiTool -> true));
    }

    public void reload() {
        xPlane.getToolsManager().reload();
    }

    @SneakyThrows
    public void installTool(Tool tool) {
        ConsoleController consoleController = displayConsole("Installing " + tool.getName());
        AsyncHelper.runAsync(() -> xPlane.getToolsManager().installTool(tool, consoleController));
    }

    @SneakyThrows
    public void uninstallTool(Tool tool) {
        ConsoleController consoleController = displayConsole("Uninstalling " + tool.getName());
        AsyncHelper.runAsync(() -> xPlane.getToolsManager().uninstallTool(tool, consoleController));
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
