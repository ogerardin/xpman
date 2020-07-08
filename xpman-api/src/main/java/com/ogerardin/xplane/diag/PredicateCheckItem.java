package com.ogerardin.xplane.diag;

import com.ogerardin.xplane.config.XPlaneInstance;
import lombok.Data;

@Data
public class PredicateCheckItem<T> implements CheckItem<T> {

    final Severity level;

    final String message;

    final Check<T> predicate;

    @Override
    public CheckResult check(T target, XPlaneInstance xPlaneInstance) {
        if (! predicate.apply(target, xPlaneInstance)) {
            return new CheckResult(level, message, null);
        }
        return new CheckResult(Severity.OK, message, null);
    }
}
