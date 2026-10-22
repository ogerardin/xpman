package com.ogerardin.xplane.exception;

/** Thrown when a requested operation is not allowed in the current state. */
public class IllegalOperation extends Exception {
    public IllegalOperation(String message) {
        super(message);
    }
}
