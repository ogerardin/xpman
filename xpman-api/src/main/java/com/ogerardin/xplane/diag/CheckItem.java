package com.ogerardin.xplane.diag;

import com.ogerardin.xplane.config.XPlaneInstance;

public interface CheckItem<T> {

    CheckResult check(T target, XPlaneInstance xPlaneInstance);
}
