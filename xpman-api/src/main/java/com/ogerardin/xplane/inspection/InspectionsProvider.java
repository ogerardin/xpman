package com.ogerardin.xplane.inspection;

import com.ogerardin.xplane.config.XPlaneInstance;

public interface InspectionsProvider<T> {

    Inspections<T> getInspections(XPlaneInstance xPlaneInstance);
}
