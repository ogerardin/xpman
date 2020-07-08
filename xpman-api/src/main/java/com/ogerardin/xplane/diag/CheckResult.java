package com.ogerardin.xplane.diag;

import lombok.Data;

import java.util.List;

@Data
public class CheckResult {

    final Severity level;

    final String message;

    final List<Fix> fixes;
}
