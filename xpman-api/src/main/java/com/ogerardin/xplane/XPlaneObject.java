package com.ogerardin.xplane;

import lombok.Data;
import lombok.ToString;

/**
 * Abstract base class for domain objects that belong to a specific X-Plane installation.
 *
 * <p>Subclasses represent entities such as aircraft, plugins, or scenery packages and
 * inherit a back-reference to their owning {@link XPlane} instance.</p>
 */
@Data
public abstract class XPlaneObject {

    @ToString.Exclude
    private final XPlane xPlane;
}
