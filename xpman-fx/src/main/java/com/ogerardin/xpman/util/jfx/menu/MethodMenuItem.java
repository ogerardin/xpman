package com.ogerardin.xpman.util.jfx.menu;

import com.ogerardin.xpman.util.SpelUtil;
import com.ogerardin.xpman.util.jfx.menu.annotation.Confirm;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import com.ogerardin.xpman.util.jfx.menu.annotation.OnSuccess;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * A {@link MenuItem} that, when activated, triggers the execution of a {@link Method}. The execution can be
 * controlled by annotations on the method, such as @{@link EnabledIf}, @{@link Confirm} or @{@link OnSuccess}.
 *
 * @param <T> type of the {@link Method}'s target
 */
@Slf4j
public class MethodMenuItem<T> extends MenuItem implements Refreshable {

    private final String enabledIfExpr;
    private final T target;

    public MethodMenuItem(Object evalContextRoot, String text, Method method, T target, Object... paramValues) {
        super(text);
        this.target = target;

        EnabledIf enabledIf = method.getAnnotation(EnabledIf.class);
        this.enabledIfExpr = (enabledIf != null) ? enabledIf.value() : null;

        MethodActionConfigurer<T, ?> configurer = new MethodActionConfigurer<>(
                () -> getParentPopup().getOwnerWindow(),
                method, evalContextRoot, target, paramValues);

        MethodAction<T, ?> action = configurer.getMethodAction();

        setOnAction(event -> action.run());
    }

    @Override
    public void refresh() {
        if (enabledIfExpr != null) {
            Boolean enabled = (Boolean) SpelUtil.eval(enabledIfExpr, target);
            setVisible((enabled != null) && enabled);
        }

    }

}
