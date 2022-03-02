package com.ogerardin.xpman.util.jfx.panels.menu;

import javafx.scene.control.Alert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the action associated with the annotated method requires user confirmation before being executed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Confirm {

    /**
     * SpEL expression defining the confirmation message that will be presented to the user.
     * The expression is evaluated with the target object as context root.
     */
    String value() default "'Are you sure?'";

    Alert.AlertType alertType() default Alert.AlertType.CONFIRMATION;
}
