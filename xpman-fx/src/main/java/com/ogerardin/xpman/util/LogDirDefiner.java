package com.ogerardin.xpman.util;

import ch.qos.logback.core.PropertyDefinerBase;

/** Resolves the platform-specific log directory while Logback reads its configuration. */
public class LogDirDefiner extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        return Logs.logDir().toString();
    }
}
