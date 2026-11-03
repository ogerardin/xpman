package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xpman.util.jfx.cell_factory.SwitchRow;

/**
 * Common interface for plugin table rows (plugins and scripts).
 */
public interface PluginRow extends SwitchRow {
    
    String getName();
    
    String getDesc();
    
    String getVersion();
    
    String getLatestVersion();

    default String getUpdateTooltip() {
        return "";
    }
    
    boolean isUpdateAvailable();

    boolean getSystem();
    
    boolean isScript();

    default boolean isGroupHeader() {
        return false;
    }

    default String getUpdateActionLabel() { return "Update…"; }

    /** Starts the provider's flow; only rows advertising updates can trigger it. */
    default void update() {
    }

    @Override
    default boolean isSwitchLocked() {
        return getSystem();
    }

    @Override
    default boolean isSwitchHidden() {
        return isGroupHeader();
    }
}
