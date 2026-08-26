package com.ogerardin.test.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that marks a test-gating annotation as carrying a file path
 * (via its {@code value()} attribute) to be checked for presence by {@link FilePresentCondition}.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilePresenceCheck {
}
