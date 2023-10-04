package com.ogerardin.xpman.util.jfx.menu;

import com.ogerardin.xpman.util.SpelUtil;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import javafx.scene.control.Hyperlink;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 */
@Slf4j
public class MethodHyperlink<T> extends Hyperlink implements Refreshable {

    private final String enabledIfExpr;
    private final T target;

    public MethodHyperlink(String text, Method method, Object evalContextRoot, T target, Object... paramValues) {
        super(text);
        this.target = target;

        getStylesheets().add(MethodHyperlink.class.getResource("/css/hyperlink.css").toExternalForm());

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
