package com.ogerardin.xplane.diag;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CheckResult {

    final Severity severity;

    final String source;

    final String message;

    final List<Fix> fixes;

    public CheckResult(Severity severity, String message) {
        this(severity, null, message, null);
    }

    public CheckResult(Severity severity, String source, String message) {
        this(severity, source, message, null);
    }
}
