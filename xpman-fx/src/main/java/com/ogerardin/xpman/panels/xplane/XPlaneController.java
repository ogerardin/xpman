package com.ogerardin.xpman.panels.xplane;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.laminar.UpdateInformation;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xplane.util.platform.Platforms;
import com.sun.jna.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.util.Duration;
import lombok.SneakyThrows;
import org.controlsfx.control.Notifications;

/**
 * Controller for the first tab pane, which contains a summary of X-Plane installation.
 */
public class XPlaneController {

    private XPlane xPlane;

    @FXML
    private Label appPath;

    @FXML
    private Button startXPlaneButton;

    @FXML
    private Hyperlink folder;

    @FXML
    private Label version;

    @FXML
    private Hyperlink log;

    public XPlaneController(XPmanFX mainController) {
        mainController.xPlaneProperty().addListener((observable, oldValue, xPlane) -> {
            this.xPlane = xPlane;
            updateDisplay(xPlane);
            checkUpdates(xPlane);
        });
    }

    private void checkUpdates(XPlane xPlane) {
        final String latestBeta = UpdateInformation.getLatestBeta();
        final String latestFinal = UpdateInformation.getLatestFinal();

        final String currentVersion = xPlane.getVersion();
        if (currentVersion != null) {
            if (latestFinal.compareToIgnoreCase(currentVersion) > 0) {
                // newer final
                Notifications.create()
                        .title("New release version")
                        .text("Release version " + latestBeta + " is available (you have " + currentVersion + ")")
                        .hideAfter(Duration.seconds(30.0))
                        .position(Pos.TOP_RIGHT)
                        .showInformation();
            }
            if (! latestBeta.equals(latestFinal) && latestBeta.compareToIgnoreCase(currentVersion) > 0) {
                // newer beta
                Notifications.create()
                        .title("New beta version")
                        .text("Beta version " + latestBeta + " is available (you have " + currentVersion + ")")
                        .hideAfter(Duration.seconds(30.0))
                        .position(Pos.TOP_RIGHT)
                        .showInformation();
            }
        }
    }

    private void updateDisplay(XPlane xPlane) {
        version.setText(String.format("%s (%s)", xPlane.getVersion(), xPlane.getVariant().name()));
        folder.setText(xPlane.getBaseFolder().toString());
        appPath.setText(xPlane.getAppPath().toString());
        log.setText(xPlane.getLogPath().toString());
        // disable "start" button if current platform different from X-Plane detected platform
        startXPlaneButton.setDisable(Platform.getOSType() != xPlane.getVariant().getOsType());
    }

    @SneakyThrows
    public void showFolder() {
        Platforms.getCurrent().reveal(xPlane.getAppPath());
    }

    public void startXPlane() {
        Platforms.getCurrent().startApp(xPlane.getAppPath());
    }

    @FXML
    private void showLog() {
        Platforms.getCurrent().openFile(xPlane.getLogPath());

    }
}
