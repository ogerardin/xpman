package com.ogerardin.xpman.util.jfx.panels.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method is to generate a group of actions, one for each item in the Iterable returned
 * by evaluating a specified expression on the target object.
 * Each action name can be customized by using {@link #itemLabel}. The actual arguments passed to the method
 * when the action is invoked are defined by using @{@link Value} on each parameter.
 * <br><br>
 * The following example configures a method to generate a group of actions as follows:
 * <ul>
 *     <li>the group will be named "Links"</li>
 *     <li>the target object is expected to have a property named "links" which is a {@link java.util.Map}</li>
 *     <li>one action will be generated for each map entry</li>
 *     <li>the action label will be the entry key</li>
 *     <li>when the action is triggered, the method {@code openLink} will be invoked with the entry value as argument</li>
 * </ul>
 * <pre>@ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
public void openLink(@Value("#item.value") URL url)  { ... }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ForEach {

    /**
     * SpEL expression expected to return an Iterable. One action will be generated for each item of the Iterable.
     * The expression is evaluated with the target object as context root.
     */
    String iterable();

    /** Name of the group */
    String group();

    /** Name of the variable that represents the current Iterable item in the {@link ForEach#itemLabel} and
     * {@link Value#value} expressions. Default: "item" */
    String itemVariableName() default "item";

    /** The action name as a SpEL expression. Evaluated with the target object as context root and the current
     * Iterable item in a variable named as specified by {@link #itemVariableName()} */
    String itemLabel() default "item.toString()";
}
