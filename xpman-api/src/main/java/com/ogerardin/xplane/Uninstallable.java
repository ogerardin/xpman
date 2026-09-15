package com.ogerardin.xplane;

import java.io.IOException;

/**
 * Interface for objects that can be uninstalled.
 */
public interface Uninstallable {
    
    /**
     * Uninstall this object (typically by moving to trash).
     * 
     * @throws IOException if uninstallation fails
     */
    void uninstall() throws IOException;
}
