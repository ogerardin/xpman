package com.ogerardin.xpman.diag;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xpman.util.jfx.TableColumnWidthHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.util.List;

/** FXML controller for the inspection-results dialog, displaying messages in a table. */
public class DiagController {

    private static final Label PLACEHOLDER = new Label("No messages");

    @FXML
    private TableView<InspectionMessage> tableView;

    public void setItems(List<InspectionMessage> items) {
        tableView.getItems().setAll(items);
    }

    @FXML
    private void initialize() {
        TableColumnWidthHelper.setup("inspectionResults", tableView.getColumns());
        tableView.setPlaceholder(PLACEHOLDER);
    }

}
