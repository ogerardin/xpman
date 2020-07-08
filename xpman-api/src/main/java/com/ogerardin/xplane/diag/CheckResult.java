package com.ogerardin.xplane.diag;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CheckResult {

    final Severity severity;

    final String message;

    final List<Fix> fixes;

    public CheckResult(Severity severity, String message) {
        this(severity, message, null);
    }
}
