package com.ogerardin.xplane.tools;

/** Thrown when a tool operation (install, uninstall, launch) fails. */
public class ToolsException extends Exception {
    public ToolsException(Exception e) {
        super(e);
    }
}
