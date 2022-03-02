package com.ogerardin.xpman.util.jfx.panels.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a value for an action based on the annotated method. Used in association with {@link ForEach}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Value {

    /** SpEL expression used to compute the value of the argument passed to method ivocation.
     * The expression is evaluated with the target object as context root.
     */
    String value();
}
