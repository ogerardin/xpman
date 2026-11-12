package com.ogerardin.xpman.diag;

import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTableRowFactory;
import com.ogerardin.xpman.util.jfx.TableColumnWidthHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.util.List;

/** FXML controller for the inspection-results dialog, displaying messages in a table. */
public class DiagController {

    private static final Label PLACEHOLDER = new Label("No messages");

    private final ObservableList<UiInspectionMessage> messages = FXCollections.observableArrayList();
    private final FilteredList<UiInspectionMessage> filteredMessages = new FilteredList<>(messages);

    @FXML
    private TableView<UiInspectionMessage> tableView;
    @FXML
    private TextArea detailsArea;
    @FXML
    private ToggleButton allButton;
    @FXML
    private ToggleButton errorButton;
    @FXML
    private ToggleButton warningButton;
    @FXML
    private ToggleButton infoButton;

    public void setItems(List<UiInspectionMessage> items) {
        messages.setAll(items);
    }

    @FXML
    private void initialize() {
        TableColumnWidthHelper.setup("inspectionResults", tableView.getColumns());
        tableView.setPlaceholder(PLACEHOLDER);
        tableView.setItems(filteredMessages);
        tableView.setRowFactory(new IntrospectingContextMenuTableRowFactory<>(this));

        ToggleGroup severityFilter = new ToggleGroup();
        allButton.setToggleGroup(severityFilter);
        errorButton.setToggleGroup(severityFilter);
        warningButton.setToggleGroup(severityFilter);
        infoButton.setToggleGroup(severityFilter);
        allButton.setUserData(null);
        errorButton.setUserData(Severity.ERROR);
        warningButton.setUserData(Severity.WARN);
        infoButton.setUserData(Severity.INFO);
        allButton.setSelected(true);
        severityFilter.selectedToggleProperty().addListener((_, _, selected) -> {
            Severity severity = selected == null ? null : (Severity) selected.getUserData();
            filteredMessages.setPredicate(message -> severity == null || message.getSeverity() == severity);
        });
        tableView.getSelectionModel().selectedItemProperty().addListener((_, _, selected) ->
                detailsArea.setText(selected == null ? "" : selected.getDetailsText()));
    }

}
