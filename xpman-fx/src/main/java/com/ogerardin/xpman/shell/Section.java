package com.ogerardin.xpman.shell;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The navigation sections of the main window, mapped to their sidebar entry (label + Ikonli icon literal)
 * and the FXML view displayed in the content area when selected.
 */
@RequiredArgsConstructor
@Getter
public enum Section {
    HOME("Home", "fth-home", "/fxml/panels/xplane.fxml"),
    AIRCRAFT("Aircraft", "fth-send", "/fxml/panels/aircraft.fxml"),
    SCENERY("Scenery", "fth-map", "/fxml/panels/scenery.fxml"),
    NAV_DATA("Nav data", "fth-navigation", "/fxml/panels/navdata.fxml"),
    PLUGINS("Plugins", "fth-package", "/fxml/panels/plugins.fxml"),
    TOOLS("Tools", "fth-tool", "/fxml/tools/tools.fxml");

    private final String label;
    private final String iconLiteral;
    private final String contentFxml;
}
