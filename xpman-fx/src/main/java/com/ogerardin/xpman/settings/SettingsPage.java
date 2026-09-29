package com.ogerardin.xpman.settings;

/**
 * Implemented by settings pane controllers that hold editable preference state;
 * {@link #apply()} is called when the user clicks OK in the Settings dialog.
 */
public interface SettingsPage {
    void apply();
}
