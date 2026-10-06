package com.ogerardin.xpman.panels.plugins;

import java.io.IOException;

/**
 * Common interface for plugin table rows (plugins and scripts).
 */
public interface PluginRow {
    
    String getName();
    
    String getDesc();
    
    String getVersion();
    
    String getLatestVersion();
    
    boolean isUpdateAvailable();
    
    boolean isEnabled();

    void setEnabled(boolean enabled) throws IOException;
    
    boolean getSystem();
    
    boolean isScript();

    default boolean isGroupHeader() {
        return false;
    }
}
