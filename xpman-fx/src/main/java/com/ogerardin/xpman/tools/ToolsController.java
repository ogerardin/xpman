package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.panels.Controller;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.util.jfx.EmptyState;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ToolsController extends Controller {

    private final XPlaneProperty xPlaneProperty;

    @FXML
    private ToggleButton installedButton;

    @FXML
    private ToggleButton availableButton;

    @FXML
    private VBox cardListContainer;

    @FXML
    private ToolDetailView detailView;

    @FXML
    private StackPane placeholderPane;

    private ManagerItemsObservableList<Tool, UiTool> uiItems;
    private FilteredList<UiTool> filteredList;

    public ToolsController(XPmanFX mainController) {
        this.xPlaneProperty = mainController.xPlaneProperty();
    }

    @FXML
    public void initialize() {
        uiItems = new ManagerItemsObservableList<>(
                xPlaneProperty,
                XPlane::getToolsManager,
                tool -> new UiTool(tool, xPlaneProperty.get())
        );

        filteredList = new FilteredList<>(uiItems);
        filteredList.addListener((ListChangeListener<UiTool>) __ -> updateCardList());

        // initially display installed tools only (simulate click on "Installed" button)
        installedButton.setSelected(true);
        installedButton.fire();
    }

    private void updateCardList() {
        cardListContainer.getChildren().clear();
        for (UiTool uiTool : filteredList) {
            ToolCardView card = new ToolCardView(uiTool, this);
            card.setOnMouseClicked(__ -> detailView.setTool(uiTool));
            cardListContainer.getChildren().add(card);
        }

        boolean empty = filteredList.isEmpty();
        placeholderPane.setVisible(empty);
        placeholderPane.setManaged(empty);
        if (empty) {
            placeholderPane.getChildren().setAll(new EmptyState("fth-tool", "No tools to show"));
        }

        if (!filteredList.isEmpty()) {
            detailView.setTool(filteredList.get(0));
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
        uiItems.reload();
    }
}
