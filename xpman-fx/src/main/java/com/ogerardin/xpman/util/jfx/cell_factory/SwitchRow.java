package com.ogerardin.xpman.util.jfx.cell_factory;

import java.io.IOException;

/**
 * A table row that can be rendered with an interactive enable/disable {@link org.controlsfx.control.ToggleSwitch}
 * in a table column. Implementations expose the current state and a mutator; the switch factory handles the
 * rest (grey-out for locked rows, hide for header rows, revert + error dialog on failure).
 */
public interface SwitchRow {

    boolean isEnabled();

    void setEnabled(boolean enabled) throws IOException;

    /** Switch displayed but locked (e.g. system plugins, not-listed scenery entries). */
    default boolean isSwitchLocked() {
        return false;
    }

    /** Switch hidden altogether (e.g. group header rows). */
    default boolean isSwitchHidden() {
        return false;
    }
}
