package com.ogerardin.xpman;

import com.ogerardin.xplane.XPlane;
import javafx.beans.property.SimpleObjectProperty;

/** A JavaFX object property holding the currently open {@link XPlane} instance. */
@SuppressWarnings("java:S110") // This alias necessarily inherits JavaFX's property hierarchy.
public class XPlaneProperty extends SimpleObjectProperty<XPlane> {
}
