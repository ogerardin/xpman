package com.ogerardin.xpman.util.panels.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method is to generate several "actions". This is mostly useful when the method has
 * parameters, in association with @{@link Value}, to generate actions with different parameter values.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ForEach {

    /**
     * Iterable expression. An action will be generated for each of the Iterable items.
     * The expression is evaluated with the target object as context root.
     */
    String iterable();

    /** Name of the group */
    String group();

    /** Name of the variable that represents the current Iterable item in the {@link ForEach#itemLabel} and
     * {@link Value#value} expressions. Default: "item" */
    String itemVariableName() default "item";

    /** String expression to be used as action name. Evaluated with the target object as context root and the current
     * Iterable item in a variable named {@link ForEach#itemLabel} */
    String itemLabel() default "item.toString()";
}
