package com.ogerardin.xpman.util.jfx.panels.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the action associated with the annotated method is onlay available if a specific expression evaluates
 * to true.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EnabledIf {

    /**
     * SpEL expression used to determine if the annotated method is available.
     * The expression is evaluated with the target object as context root and is expected to evaluata as a boolean.
     */
    String value();
}
