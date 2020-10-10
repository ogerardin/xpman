package com.ogerardin.xpman.util.jfx.panels.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method is to generate one action for each item of the specified iterable.
 * The action name can be customized by using {@link #itemLabel()}. The parameter values associated with a
 * specific item are customized by using @{@link Value} on each parameter.
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
     * Iterable item in a variable named "itemLabel" */
    String itemLabel() default "item.toString()";
}
