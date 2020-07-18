package com.ogerardin.xplane.inspection;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class InspectionMessage {

    final Severity severity;

    final String source;

    final String message;

    final List<Fix> fixes;

    public InspectionMessage(Severity severity, String message) {
        this(severity, null, message, null);
    }

    public InspectionMessage(Severity severity, String source, String message) {
        this(severity, source, message, null);
    }
}
