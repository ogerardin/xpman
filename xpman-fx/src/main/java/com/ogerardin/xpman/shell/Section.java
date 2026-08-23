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
    HOME("Home", "fe-home", "/fxml/panels/xplane.fxml"),
    AIRCRAFT("Aircraft", "fe-plane", "/fxml/panels/aircraft.fxml"),
    SCENERY("Scenery", "fe-map", "/fxml/panels/scenery.fxml"),
    NAV_DATA("Nav data", "fe-navigation", "/fxml/panels/navdata.fxml"),
    PLUGINS("Plugins", "fe-plug", "/fxml/panels/plugins.fxml"),
    TOOLS("Tools", "fe-tool", "/fxml/tools/tools.fxml");

    private final String label;
    private final String iconLiteral;
    private final String contentFxml;
}
