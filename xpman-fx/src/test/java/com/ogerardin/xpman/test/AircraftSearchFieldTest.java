package com.ogerardin.xpman.test;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.panels.aircraft.AircraftsController;
import com.ogerardin.xpman.util.jfx.Filter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Exercises the aircraft search control loaded from the production FXML and stylesheets. */
class AircraftSearchFieldTest {
    @BeforeAll
    static void startToolkit() {
        assumeTrue(!System.getProperty("os.name").startsWith("Linux") || System.getenv("DISPLAY") != null,
                "JavaFX requires a display");
        Platform.startup(() -> Platform.setImplicitExit(false));
    }

    @Test
    void embeddedClearButtonClearsSearchWithoutChangingDropdownFilter() throws Exception {
        onFxThread(() -> {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            var loader = loadAircraftPanel();
            TextField search = (TextField) loader.getNamespace().get("searchField");
            @SuppressWarnings("unchecked")
            var filters = (ComboBox<Filter<Aircraft>>) loader.getNamespace().get("filterCombo");
            var selectedFilter = new Filter<Aircraft>("No matches", _ -> false);
            filters.setValue(selectedFilter);
            // The toolbar is normally enabled by opening X-Plane; avoid scanning a real installation here.
            ToolBar toolbar = (ToolBar) loader.getNamespace().get("toolbar");
            toolbar.disableProperty().unbind();
            toolbar.setDisable(false);
            search.setText("Boeing");
            var clearButton = search.lookup(".clear-button");
            assertNotNull(clearButton, "Search must contain a clear button");
            clearButton.fireEvent(new MouseEvent(MouseEvent.MOUSE_RELEASED, 0, 0, 0, 0,
                    MouseButton.PRIMARY, 1, false, false, false, false,
                    false, false, false, false, false, true, null));
            assertEquals("", search.getText());
            assertSame(selectedFilter, filters.getValue());
            return null;
        });
    }

    @Test
    void searchCssResolvesInLightAndDarkThemes() throws Exception {
        onFxThread(() -> {
            var warnings = new ArrayList<String>();
            var logger = Logger.getLogger("javafx.css");
            var handler = new Handler() {
                @Override public void publish(LogRecord record) { warnings.add(record.getMessage()); }
                @Override public void flush() {}
                @Override public void close() {}
            };
            logger.addHandler(handler);
            String previousTheme = Application.getUserAgentStylesheet();
            try {
                for (var theme : new String[]{new PrimerLight().getUserAgentStylesheet(),
                        new PrimerDark().getUserAgentStylesheet()}) {
                    Application.setUserAgentStylesheet(theme);
                    var loader = loadAircraftPanel();
                    Parent root = loader.getRoot();
                    TextField search = (TextField) loader.getNamespace().get("searchField");
                    search.setText("Boeing");
                    root.applyCss();
                    search.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("focused"), true);
                    root.applyCss();
                }
                assertTrue(warnings.isEmpty(), () -> String.join("\n", warnings));
            } finally {
                Application.setUserAgentStylesheet(previousTheme);
                logger.removeHandler(handler);
            }
            return null;
        });
    }

    private static FXMLLoader loadAircraftPanel() throws Exception {
        var app = mock(XPmanFX.class);
        when(app.xPlaneProperty()).thenReturn(new XPlaneProperty());
        var loader = new FXMLLoader(Path.of("src/main/resources/fxml/panels/aircraft.fxml").toUri().toURL());
        loader.setControllerFactory(_ -> new AircraftsController(app));
        Parent root = loader.load();
        var scene = new Scene(root);
        scene.getStylesheets().add(Path.of("src/main/resources/css/xpman.css").toUri().toString());
        root.applyCss();
        return loader;
    }

    private static <T> T onFxThread(Callable<T> action) throws Exception {
        var task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(15, TimeUnit.SECONDS);
    }
}
