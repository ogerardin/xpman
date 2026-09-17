package com.ogerardin.xplane.skunkcrafts;

/**
 * Thrown when a Skunkcrafts update operation cannot proceed because the addon
 * is not Skunkcrafts-updatable (no config, disabled, or locked).
 */
public class SkunkcraftsUpdateException extends Exception {
    
    public SkunkcraftsUpdateException(String message) {
        super(message);
    }
    
    public SkunkcraftsUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
