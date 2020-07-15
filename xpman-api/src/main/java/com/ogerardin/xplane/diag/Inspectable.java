package com.ogerardin.xplane.diag;

import com.ogerardin.xplane.config.XPlaneInstance;

import java.util.List;

public interface Inspectable {

    List<InspectionResult> inspect(XPlaneInstance xPlaneInstance);

}
