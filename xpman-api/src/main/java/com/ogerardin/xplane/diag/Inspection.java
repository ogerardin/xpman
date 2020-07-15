package com.ogerardin.xplane.diag;

import com.ogerardin.xplane.config.XPlaneInstance;

import java.util.List;

public interface Inspection<T> {

    List<InspectionResult> inspect(T target, XPlaneInstance xPlaneInstance);
}
