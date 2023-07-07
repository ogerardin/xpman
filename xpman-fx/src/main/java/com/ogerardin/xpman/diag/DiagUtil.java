package com.ogerardin.xpman.diag;

import com.ogerardin.xplane.inspection.InspectionMessage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class DiagUtil {
    @SneakyThrows
    public void displayInspectionMessages(List<InspectionMessage> messages, Window owner) {
        FXMLLoader loader = new FXMLLoader(DiagUtil.class.getResource("/fxml/diag.fxml"));
        Pane pane = loader.load();
        DiagController controller = loader.getController();
        controller.setItems(messages);
        Stage stage = new Stage();
        stage.setTitle("Inspection results");
        stage.setScene(new Scene(pane));
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.show();
    }

    public void displayInspectionMessages(List<InspectionMessage> messages) {
        displayInspectionMessages(messages, null);
    }
}
