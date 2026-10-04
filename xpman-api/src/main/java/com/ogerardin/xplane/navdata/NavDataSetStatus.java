package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;

import java.util.Comparator;

/**
 * The overall status of a nav data layer, as a four-state traffic light.
 */
public enum NavDataSetStatus {
    /** No file present, or the layer is ignored by a higher-priority overriding layer. */
    INACTIVE,
    /** Files present, no warning and no error. */
    OK,
    /** Files present, at least one warning, no error. */
    WARNING,
    /** Files present and at least one error. */
    ERROR;

    /**
     * Derives the status of a layer. Inactivity wins over severity: X-Plane does not read
     * an ignored layer, so its messages describe files the sim never loads.
     */
    public static NavDataSetStatus of(boolean present, boolean ignored, InspectionResult result) {
        if (!present || ignored) {
            return INACTIVE;
        }
        // Severity is declared INFO < WARN < ERROR, so its natural order is worst-last.
        return switch (result.getMessages().stream()
                .map(InspectionMessage::getSeverity)
                .max(Comparator.naturalOrder())
                .orElse(Severity.INFO)) {
            case ERROR -> ERROR;
            case WARN -> WARNING;
            case INFO -> OK;
        };
    }
}