package com.ogerardin.xplane.diag;

import com.ogerardin.xplane.config.XPlaneInstance;

import java.util.List;

public interface Checkable {

    List<CheckResult> check(XPlaneInstance xPlaneInstance);

}
