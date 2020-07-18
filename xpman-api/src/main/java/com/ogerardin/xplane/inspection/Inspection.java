package com.ogerardin.xplane.inspection;

import com.ogerardin.xplane.config.XPlaneInstance;

import java.util.List;

public interface Inspection<T> {

    List<InspectionMessage> apply(T target, XPlaneInstance xPlaneInstance);
}
