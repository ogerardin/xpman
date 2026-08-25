package com.ogerardin.test.util;

import com.ogerardin.xplane.XPlaneMajorVersion;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(EnableOnLocalXPlaneVersionCondition.class)
@XPlaneVersionRequired(XPlaneMajorVersion.XP12)
public @interface EnableOnLocalXPlane12 {
}
