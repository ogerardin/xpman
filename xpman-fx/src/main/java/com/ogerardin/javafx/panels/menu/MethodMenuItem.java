package com.ogerardin.javafx.panels.menu;

import com.ogerardin.xpman.util.SpelUtil;
import javafx.scene.control.MenuItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class MethodMenuItem<T> extends MenuItem implements Contextualizable<T> {

    private final String enabledIfExpr;

    private Runnable aftermethodExecution;

    private T target;

    public MethodMenuItem(String text, Method method, T target, Object... paramValues) {
        setText(text);
        this.target = target;
        EnabledIf enabledIf = method.getAnnotation(EnabledIf.class);
        this.enabledIfExpr = (enabledIf != null) ? enabledIf.value() : null;

        setOnAction(event -> {
            try {
                method.invoke(this.getTarget(), paramValues);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Exception while invoking method", e);
            }
            if (aftermethodExecution != null) {
                aftermethodExecution.run();
            }
        });
    }

    @Override
    public void contextualize(T target) {
//        this.target = target;
        setTarget(target);

        if (enabledIfExpr != null) {
            Boolean enabled = (Boolean) SpelUtil.eval(enabledIfExpr,target);
            setVisible(enabled);
        }
    }

}
