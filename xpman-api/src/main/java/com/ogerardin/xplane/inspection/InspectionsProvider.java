package com.ogerardin.xplane.inspection;

import com.ogerardin.xplane.XPlane;

public interface InspectionsProvider<T> {

    Inspections<T> getInspections(XPlane xPlane);
}
