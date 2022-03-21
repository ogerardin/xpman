package com.ogerardin.xpman.util.jfx.menu;

import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xpman.util.SpelUtil;
import com.ogerardin.xpman.util.jfx.menu.annotation.ForEach;
import com.ogerardin.xpman.util.jfx.menu.annotation.Value;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * A {@link Menu} where sub-items are constructed by iterating over an expression provided by a {@link ForEach} annotation.
 * @param <T> type of the target object
 */
@Slf4j
public class ForEachMenuItem<T> extends Menu {

    private final ForEach forEach;

    public ForEachMenuItem(Object evalContextRoot, ForEach forEach, Method method, T target) {
        super(forEach.group());
        this.forEach = forEach;

        // extract @Value expression for each of the method's param
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        String[] paramValueExpr = new String[parameterAnnotations.length];
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            paramValueExpr[i] = Arrays.stream(annotations)
                    .filter(annotation -> annotation.annotationType() == Value.class)
                    .findAny()
                    .map(Value.class::cast)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("All parameters of method %s must be " +
                            "annotated with @Value when method is annotated with @ForEach", method)))
                    .value();
        }

        // build submenu items
        log.debug("Contextualizing {} for {}", this, target);
        getItems().clear();
        Object exprValue = SpelUtil.eval(this.forEach.iterable(), target);
        if (! (exprValue instanceof Iterable iterable)) {
            throw new IllegalArgumentException(String.format("Expected Iterable, got %s", exprValue));
        }
        for (Object item : iterable) {
            MenuItem menuItem = buildMenuItem(method, target, item, evalContextRoot, paramValueExpr);
            log.debug("Adding item {}", menuItem);
            getItems().add(menuItem);
        }

        // hide this Menu when there are no children
        visibleProperty().bind(Bindings.isNotEmpty(getItems()));
    }

    /**
     * Build a single MenuItem which, when selected, invokes the specified method with parameter values computed
     * by evaluating the associated @{@link Value} expressions.
     * @param method method to invoke
     * @param target the target object
     * @param item the iterable value associated with the current action
     */
    private MenuItem buildMenuItem(Method method, T target, Object item, Object evalContextRoot, String[] paramValueExpr) {
        Map<String, Object> contextVariables = Maps.mapOf(
                forEach.itemVariableName(), item
        );

        // compute parameter values
        Object[] paramValues = new Object[method.getParameterCount()];
        for (int i = 0; i < paramValueExpr.length; i++) {
            String expr = paramValueExpr[i];
            Object paramValue = SpelUtil.eval(expr, target, contextVariables);
            paramValues[i] = paramValue;
        }

        String itemLabelExpr = forEach.itemLabel();
        String text = (String) SpelUtil.eval(itemLabelExpr, target, contextVariables);

        return new MethodMenuItem<>(evalContextRoot, text, method, target, paramValues);
    }
}
