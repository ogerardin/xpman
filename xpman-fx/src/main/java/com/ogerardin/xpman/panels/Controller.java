package com.ogerardin.xpman.panels;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xpman.diag.DiagUtil;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public abstract class Controller {

    protected final ImageView LOADING = Optional.ofNullable(this.getClass().getResource("/img/loading.gif"))
            .map(URL::toExternalForm)
            .map(Image::new)
            .map(ImageView::new)
            .orElseThrow(() -> new RuntimeException("Could not load image"));

    @SuppressWarnings("unused")
    @SneakyThrows
    public void displayInspectionResults(List<InspectionMessage> messages) {
        DiagUtil.displayInspectionMessages(messages);
    }
}
