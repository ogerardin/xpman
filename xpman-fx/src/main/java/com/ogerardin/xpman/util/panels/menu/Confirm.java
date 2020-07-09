package com.ogerardin.xpman.util.panels.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the action associated to the annotated method requires user confirmation before being executed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Confirm {

    /**
     * String expression defining the confirmation message that will be presented to the user.
     * The expression is evaluated with the target object as context root.
     */
    String value() default "'Are you sure?'";
}
