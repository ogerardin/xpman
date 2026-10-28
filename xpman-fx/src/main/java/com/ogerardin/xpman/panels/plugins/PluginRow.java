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

    /** Runs the Skunkcrafts update wizard and reloads the manager; no-op default because only updatable rows can trigger it. */
    default void skunkcraftsUpdate() {
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
