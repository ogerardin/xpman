package com.ogerardin.xplane;

import lombok.Data;
import lombok.ToString;

@Data
public abstract class XPlaneObject {

    @ToString.Exclude
    private final XPlane xPlane;
}
