package com.ogerardin.xplane.config.aircrafts.install;

import lombok.Data;

@Data
public class CheckResult {

    final boolean valid;

    final String message;
}
