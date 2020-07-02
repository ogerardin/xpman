package com.ogerardin.xpman.panels.xplane;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xpman.XPlaneInstanceProperty;
import com.ogerardin.xpman.platform.Platforms;
import com.sun.jna.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import lombok.SneakyThrows;

/**
 * Controller for the first tab pane, which contains a summary of X-Plane installation.
 */
public class XPlaneController {

    private XPlaneInstance xPlaneInstance;

    @FXML
    private Label appPath;

    @FXML
    private Button startXPlaneButton;

    @FXML
    private Hyperlink link;

    @FXML
    private Label version;

    public XPlaneController(XPlaneInstanceProperty xPlaneInstanceProperty) {
        xPlaneInstanceProperty.addListener((observable, oldValue, newValue) -> {
            xPlaneInstance = newValue;
            version.setText(String.format("%s (%s)", newValue.getVersion(), newValue.getVariant().name()));
            link.setText(newValue.getRootFolder().toString());
            appPath.setText(newValue.getAppPath().toString());
            // disable "start" button if current platform different from X-Plane detected platform
            startXPlaneButton.setDisable(Platform.getOSType() != newValue.getVariant().getOsType());
        });
    }

    @SneakyThrows
    public void showFolder() {
        Platforms.getCurrent().reveal(xPlaneInstance.getAppPath());
    }

    public void startXPlane() {
        Platforms.getCurrent().startApp(xPlaneInstance.getAppPath());
    }
}
