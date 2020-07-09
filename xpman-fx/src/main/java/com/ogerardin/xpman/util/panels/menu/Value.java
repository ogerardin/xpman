package com.ogerardin.xpman.util.panels.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a value for an action based on the annotated method. Useful in association with {@link ForEach}.
 * @see ForEach
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Value {

    String value();
}
