package com.ogerardin.xpman.util.jfx;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@UtilityClass
public class ErrorDialog {

    @SneakyThrows
    public void showError(Throwable e, Window owner) {
        log.error("Unhandled exception: ", e);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception");
        alert.setHeaderText("An unhandled exception occurred.");

        alert.setContentText(extractShortMessage(e));

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String exceptionText = stringWriter.toString();

        {
            GridPane gridPane = new GridPane();
            gridPane.setMaxWidth(Double.MAX_VALUE);
            {
                Label label = new Label("Stack trace:");
                gridPane.add(label, 0, 0);

                TextArea textArea = new TextArea(exceptionText);
                textArea.setEditable(false);
                textArea.setWrapText(false);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);
                gridPane.add(textArea, 0, 1);
            }
            alert.getDialogPane().setExpandableContent(gridPane);
            alert.getDialogPane().setExpanded(false);
        }

        alert.initOwner(owner);
        alert.showAndWait();
    }

    private String extractShortMessage(Throwable e) {
        while (e.getCause() != null && e.getCause() != e) {
            e = e.getCause();
        }
        return e.toString();
    }

}
