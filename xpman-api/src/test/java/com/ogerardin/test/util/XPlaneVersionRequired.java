package com.ogerardin.test.util;

import com.ogerardin.xplane.XPlaneMajorVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that binds a test-gating annotation to the X-Plane major version it requires.
 * Looked up polymorphically by {@link EnableOnLocalXPlaneVersionCondition} so that adding a
 * new version annotation (e.g. {@code @EnableOnLocalXPlane13}) requires no change to the condition.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface XPlaneVersionRequired {
    XPlaneMajorVersion value();
}
