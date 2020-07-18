package com.ogerardin.xplane.inspection;

public enum Severity {
    /** no issue */
    OK,
    /** informative: no impact, recommendation */
    INFO,
    /** warning: might not perform as expected */
    WARN,
    /** error: will probably cause failures */
    ERROR
}
