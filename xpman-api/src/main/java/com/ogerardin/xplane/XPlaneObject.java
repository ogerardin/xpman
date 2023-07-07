package com.ogerardin.xplane;

import lombok.Data;
import lombok.ToString;

@Data
public class XPlaneObject {

    @ToString.Exclude
    private final XPlane xPlane;
}
