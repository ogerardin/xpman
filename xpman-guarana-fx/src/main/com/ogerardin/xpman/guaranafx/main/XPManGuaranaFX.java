package com.ogerardin.xpman.guaranafx.main;

import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import com.ogerardin.xplane.InvalidConfig;
import com.ogerardin.xplane.XPlaneInstance;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

public class XPManGuaranaFX extends Application {

    private static XPConfigManager configManager;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI for a XPConfigManager and bind it to actual instance
        JfxInstanceUI<XPConfigManager> ui = uiManager.buildInstanceUI(XPConfigManager.class);
        ui.bind(configManager);

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "Hello XPMan!");
    }

    public static void main(String[] args) {

        // instantiate seed object
        configManager = new XPConfigManager();

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }

    // a bootstrap object so that we can call methods
    public static class XPConfigManager {
        public XPlaneInstance loadFromDirectory(Path path) throws IOException, InvalidConfig {
            return new XPlaneInstance(path);
        }
    }

}
