package com.ogerardin.xpman.diag;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xpman.config.XPManPrefs;
import com.ogerardin.xpman.util.jfx.JfxAppPrefs.WindowPosition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/** Utility for opening a non-modal dialog that shows a list of inspection messages. */
@Slf4j
@UtilityClass
public class DiagUtil {
    private static XPManPrefs config;
    private static Runnable saveCallback;

    /** Initializes the diagnostic dialog with configuration and a save callback. */
    public static void initialize(XPManPrefs config, Runnable saveCallback) {
        DiagUtil.config = config;
        DiagUtil.saveCallback = saveCallback;
    }

    @SneakyThrows
    public void displayInspectionMessages(List<InspectionMessage> messages, Window owner) {
        FXMLLoader loader = new FXMLLoader(DiagUtil.class.getResource("/fxml/inspectionResults.fxml"));
        Pane pane = loader.load();
        DiagController controller = loader.getController();
        controller.setItems(messages.stream().map(message -> new UiInspectionMessage(message, owner)).toList());
        Stage stage = new Stage();
        stage.setTitle("Inspection results");
        Scene scene = new Scene(pane);
        scene.getStylesheets().add(DiagUtil.class.getResource("/css/xpman.css").toExternalForm());
        stage.setScene(scene);
        if (owner != null) {
            stage.initOwner(owner);
        }

        // Restore saved window position and size
        if (config != null && config.getDiagPosition() != null) {
            WindowPosition pos = config.getDiagPosition();
            stage.setX(pos.getX());
            stage.setY(pos.getY());
            stage.setWidth(pos.getWidth());
            stage.setHeight(pos.getHeight());
        }

        // Save window position and size on close
        stage.setOnHiding(event -> {
            if (config != null) {
                WindowPosition pos = new WindowPosition(
                    stage.getX(), stage.getY(),
                    stage.getWidth(), stage.getHeight()
                );
                config.setDiagPosition(pos);
                if (saveCallback != null) {
                    saveCallback.run();
                }
            }
        });

        stage.show();
    }

    public void displayInspectionMessages(List<InspectionMessage> messages) {
        displayInspectionMessages(messages, null);
    }
}
