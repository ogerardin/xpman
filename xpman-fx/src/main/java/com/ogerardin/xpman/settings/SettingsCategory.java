package com.ogerardin.xpman.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The categories of the Settings dialog, mapped to their entry in the category tree (label + Ikonli icon literal)
 * and the FXML view displayed in the settings pane when selected.
 */
@RequiredArgsConstructor
@Getter
public enum SettingsCategory {
    GENERAL("General", "fth-settings", "/fxml/settings/general.fxml"),
    TOOLS("Tools", "fth-tool", "/fxml/tools/tools.fxml");

    private final String label;
    private final String iconLiteral;
    private final String contentFxml;
}
