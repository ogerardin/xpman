package com.ogerardin.xpman.panels.xplane;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.laminar.UpdateInformation;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.XPmanFX;
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

    @FXML
    private Label releaseUpdate;

    @FXML
    private Label betaUpdate;

    public XPlaneController(XPmanFX mainController) {
        mainController.xPlaneProperty().addListener((observable, oldValue, xPlane) -> {
            this.xPlane = xPlane;
            updateDisplay(xPlane);
            checkUpdates(xPlane);
        });
    }

    private void checkUpdates(XPlane xPlane) {
        final String currentVersion = xPlane.getVersion();
        if (currentVersion != null) {
            String latestFinal = UpdateInformation.getLatestFinal();
            boolean hasReleaseUpdate = latestFinal.compareToIgnoreCase(currentVersion) > 0;
            if (hasReleaseUpdate) {
                releaseUpdate.setText("Release version " + latestFinal + " is available");
            }
            releaseUpdate.setVisible(hasReleaseUpdate);

            String latestBeta = UpdateInformation.getLatestBeta();
            boolean hasBetaUpdate = ! latestBeta.equals(latestFinal) && latestBeta.compareToIgnoreCase(currentVersion) > 0;
            if (hasBetaUpdate) {
                betaUpdate.setText("Beta version " + latestBeta + " is available");
            }
            betaUpdate.setVisible(hasBetaUpdate);
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
