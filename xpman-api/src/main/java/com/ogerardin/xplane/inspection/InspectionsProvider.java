package com.ogerardin.xplane.inspection;

import com.ogerardin.xplane.XPlaneInstance;

public interface InspectionsProvider<T> {

    Inspections<T> getInspections(XPlaneInstance xPlaneInstance);
}
