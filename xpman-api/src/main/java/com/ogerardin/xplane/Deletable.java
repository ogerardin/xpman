package com.ogerardin.xplane;

import java.io.IOException;

/**
 * Interface for objects that can be deleted (moved to trash).
 */
public interface Deletable {
    
    /**
     * Delete this object (typically by moving to trash).
     * 
     * @throws IOException if deletion fails
     */
    void delete() throws IOException;
}
