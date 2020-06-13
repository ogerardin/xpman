package com.ogerardin.xpman.panels.xplane;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xpman.XPlaneInstanceProperty;
import com.ogerardin.xpman.platform.Platforms;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import lombok.SneakyThrows;

public class XPlaneController {

    private XPlaneInstance xPlaneInstance;

    @FXML
    private Hyperlink link;

    @FXML
    private Label version;

    public XPlaneController(XPlaneInstanceProperty xPlaneInstanceProperty) {
        xPlaneInstanceProperty.addListener((observable, oldValue, newValue) -> {
            xPlaneInstance = newValue;
            version.setText(String.format("%s (%s)", newValue.getVersion(), newValue.getVariant().name()));
            link.setText(newValue.getRootFolder().toString());
        });
    }

    @SneakyThrows
    public void showFolder() {
        Platforms.getCurrent().reveal(xPlaneInstance.getExePath());
    }
}
