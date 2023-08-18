package com.ogerardin.xpman.util.jfx.console;

import com.ogerardin.xplane.util.progress.ProgressListener;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class ConsoleController implements ProgressListener {

    @FXML
    private DialogPane dialogPane;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label messageLabel;
    @FXML
    private TextArea textArea;
    @FXML
    private ProgressBar progressBar;

    public ConsoleController() {
    }

    @FXML
    public void initialize() {
        textArea.clear();
        // the progress indicator will be spinning whenever progress is not 100%
        progressIndicator.progressProperty().bind(
                Bindings.when(progressBar.progressProperty().isEqualTo(1.0, 0.0))
                        .then(1.0)
                        .otherwise(-1.0)
        );
        // the Cancel buttin will be disabled when the progress is 100%
        dialogPane.lookupButton(ButtonType.CANCEL).disableProperty().bind(progressBar.progressProperty().isEqualTo(1.0, 0.0));
    }


    @Override
    public void progress(Double percent, String message) {
        Platform.runLater(() -> {
            Optional.ofNullable(percent).ifPresent(progressBar::setProgress);
            Optional.ofNullable(message).ifPresent(messageLabel::setText);
        });
    }

    @Override
    public void output(@NonNull String message) {
        log.debug(">> {}", message);
        Platform.runLater(() -> textArea.appendText(message + "\n"));
    }

    public void close() {
    }
}
