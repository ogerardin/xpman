package com.ogerardin.xplane.inspection;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class InspectionMessage {

    @Builder.Default
    private final Severity severity = Severity.INFO;

    private final String object;

    private final String message;

    @Singular
    private final List<Fix> fixes;

    /** When true, indicates that the following inspections should be skipped when this inspection
     * is performed as part of {@link Inspections#inspect} */
    @Builder.Default
    private final boolean abort = false;

    public boolean isError() {
        return severity == Severity.ERROR;
    }

}
