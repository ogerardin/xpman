package com.ogerardin.xplane.exception;

/** Thrown when the X-Plane installation configuration is invalid (e.g. folder does not exist). */
public class InvalidConfig extends Exception {
    public InvalidConfig(String message) {
        super(message);
    }
}
