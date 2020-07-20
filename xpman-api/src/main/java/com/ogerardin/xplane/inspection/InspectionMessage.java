package com.ogerardin.xplane.inspection;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class InspectionMessage {

    final Severity severity;

    final String object;

    final String message;

    final List<Fix> fixes;

    public InspectionMessage(Severity severity, String message) {
        this(severity, null, message, null);
    }

    public InspectionMessage(Severity severity, String object, String message) {
        this(severity, object, message, null);
    }
}
