package com.ogerardin.javafx.panels.menu;

import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xpman.util.SpelUtil;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

@Slf4j
public class GroupMenuItem<T> extends Menu implements Contextualizable<T> {

    private final ForEach forEach;
    private final Method method;
    private final String[] paramValueExpr;

    public GroupMenuItem(ForEach forEach, Method method) {
        super(forEach.group());
        this.forEach = forEach;
        this.method = method;

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        this.paramValueExpr = new String[parameterAnnotations.length];

        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            this.paramValueExpr[i] = Arrays.stream(annotations)
                    .filter(annotation -> annotation.annotationType() == Value.class)
                    .findAny()
                    .map(Value.class::cast)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("All parameters of method %s must be annotated with @Value when method is annotated with @ForEach", method)))
                    .value();
        }

        // show group item only when there are children
        visibleProperty().bind(Bindings.isNotEmpty(getItems()));
    }

    @Override
    public void contextualize(T target) {
        log.debug("Contextualizing {} for {}", this, target);
        getItems().clear();
        Object exprValue = SpelUtil.eval(forEach.iterable(), target);
        if (! (exprValue instanceof Iterable)) {
            throw new IllegalArgumentException(String.format("Expected Iterable, got %s", exprValue));
        }
        Iterable<?> iterable = (Iterable<?>) exprValue;
        for (Object item : iterable) {
            MenuItem menuItem = buildMenuItem(method, target, item);
            log.debug("Adding item {}", menuItem);
            getItems().add(menuItem);
        }

    }

    private MenuItem buildMenuItem(Method method, T target, Object item) {
        Map<String, Object> contextVariables = Maps.mapOf(forEach.itemVariableName(), item);

        Object[] paramValues = new Object[method.getParameterCount()];
        for (int i = 0; i < paramValueExpr.length; i++) {
            String expr = paramValueExpr[i];
            Object paramValue = SpelUtil.eval(expr, target, contextVariables);
            paramValues[i] = paramValue;
        }

        String itemLabelExpr = forEach.itemLabel();
        String text = (String) SpelUtil.eval(itemLabelExpr, target, contextVariables);

        MethodMenuItem<T> menuItem = new MethodMenuItem<>(text, method, target, paramValues);
//        MenuItem menuItem = new MenuItem(text);
        return menuItem;
    }
}
