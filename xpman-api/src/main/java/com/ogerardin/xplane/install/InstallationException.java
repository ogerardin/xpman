package com.ogerardin.xplane.install;

import java.io.IOException;

/**
 * Exception thrown when installation fails.
 * Wraps IOException to provide a domain-specific exception type.
 */
public class InstallationException extends Exception {
    
    public InstallationException(String message) {
        super(message);
    }
    
    public InstallationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InstallationException(IOException cause) {
        super(cause);
    }
}
