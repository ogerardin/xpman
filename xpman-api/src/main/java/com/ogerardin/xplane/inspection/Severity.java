package com.ogerardin.xplane.inspection;

/**
 * The severity of an {@link InspectionMessage}
 */
public enum Severity {
    /** informative: no impact, recommendation */
    INFO,
    /** warning: might not perform as expected */
    WARN,
    /** error: will probably cause failures */
    ERROR
}
