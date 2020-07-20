package com.ogerardin.xpman.util.jfx.panels.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the conditional availability of a method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EnabledIf {

    /**
     * Boolean expression to determine if the annotated method is applicable.
     * The expression is evaluated with the target object as context root.
     */
    String value();
}
