package com.ogerardin.xpman.util.jfx.menu;

import com.ogerardin.xpman.util.SpelUtil;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import javafx.scene.control.Button;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 */
@Slf4j
public class MethodButton<T> extends Button implements Refreshable {

    private final String enabledIfExpr;
    private final T target;

    public MethodButton(String text, Method method, Object evalContextRoot, T target, Object... paramValues) {
        super(text);
        this.target = target;

        EnabledIf enabledIf = method.getAnnotation(EnabledIf.class);
        this.enabledIfExpr = (enabledIf != null) ? enabledIf.value() : null;

        MethodActionConfigurer<T, ?> configurer = new MethodActionConfigurer<>(
                () -> getScene().getWindow(),
                method, evalContextRoot, target, paramValues);

        MethodAction<T, ?> action = configurer.getMethodAction();

        setOnAction(__ -> action.run());
    }



    @Override
    public void refresh() {
        if (enabledIfExpr != null) {
            Boolean enabled = (Boolean) SpelUtil.eval(enabledIfExpr, target);
            setVisible(enabled);
        }
    }

}
