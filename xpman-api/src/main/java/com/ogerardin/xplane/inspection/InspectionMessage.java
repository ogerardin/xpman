package com.ogerardin.xplane.inspection;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * A message produced as the result of running an {@link Inspection}.
 * Includes a {@link Severity}, a text message, and possibly a list of {@link Fix}es.
 */
@Data
@Builder
public class InspectionMessage {

    @Builder.Default
    private final Severity severity = Severity.INFO;

    private final String object;

    private final String message;

    private final String details;

    @Singular
    private final List<Fix> fixes;

    /** When true, indicates that the following inspections should be skipped when this inspection
     * is performed as part of a sequence of inspections */
    @Builder.Default
    private final boolean abort = false;

    public boolean isError() {
        return severity == Severity.ERROR;
    }

    public static InspectionMessage warn(String message) {
        return builder().severity(Severity.WARN).message(message).build();
    }

    public static InspectionMessage info(String message) {
        return builder().message(message).build();  // INFO is the default severity
    }

    public static InspectionMessage error(String message) {
        return builder().severity(Severity.ERROR).message(message).build();
    }

    public static InspectionMessage error(String message, boolean abort) {
        return builder().severity(Severity.ERROR).message(message).abort(abort).build();
    }

}
