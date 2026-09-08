package com.ogerardin.xpman.panels.plugins;

/**
 * Common interface for plugin table rows (plugins and scripts).
 */
public interface PluginRow {
    
    String getName();
    
    String getDesc();
    
    String getVersion();
    
    String getLatestVersion();
    
    boolean isEnabled();
    
    boolean getSystem();
    
    boolean isScript();
}
