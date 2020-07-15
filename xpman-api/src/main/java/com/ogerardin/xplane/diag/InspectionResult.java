package com.ogerardin.xplane.diag;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class InspectionResult {

    final Severity severity;

    final String source;

    final String message;

    final List<Fix> fixes;

    public InspectionResult(Severity severity, String message) {
        this(severity, null, message, null);
    }

    public InspectionResult(Severity severity, String source, String message) {
        this(severity, source, message, null);
    }
}
