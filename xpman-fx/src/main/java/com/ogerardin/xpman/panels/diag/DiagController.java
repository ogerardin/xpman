package com.ogerardin.xpman.panels.diag;

import com.ogerardin.xplane.diag.CheckResult;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import java.util.List;

public class DiagController {

    @FXML
    private TableView<CheckResult> tableView;

    public void setItems(List<CheckResult> items) {
        tableView.getItems().setAll(items);
    }

}
