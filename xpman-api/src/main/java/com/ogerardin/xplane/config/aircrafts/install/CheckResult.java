package com.ogerardin.xplane.config.aircrafts.install;

import lombok.Data;

@Data
public class CheckResult {

    final Status status;

    final String message;

    public enum Status {
        OK,
        WARN,
        ERROR;
    }
}
