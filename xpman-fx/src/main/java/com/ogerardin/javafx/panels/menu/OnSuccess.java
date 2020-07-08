package com.ogerardin.javafx.panels.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When present, this annotation indicates that an action is to be performed after the method has been successfully
 * invoked.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnSuccess {

    /** Expression to evaluate after the method has been successfully invoked.
     * The expression is evaluated with the target object as context root and a variable named {@link #resultVariableName()}
     * containing the result of the method invocation. */
    String value();

    /** Name of the variable that represents the result of the method invocation. Default: "result" */
    String resultVariableName() default "result";

}
