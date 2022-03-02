package com.ogerardin.xpman.util.jfx.panels.menu;

import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xpman.util.SpelUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link MenuItem} that, when activated, triggers the execution of a {@link Method}. The execution can be
 * controlled by annotations on the method, such as @{@link EnabledIf}, @{@link Confirm} or @{@link OnSuccess}.
 *
 * @param <T> type of the {@link Method}'s target
 */
@Slf4j
public class MethodMenuItem<T> extends MenuItem implements Contextualizable<T> {

    private final String enabledIfExpr;

    @Setter @Getter
    private T target;

    public MethodMenuItem(Object evalContextRoot, String text, Method method, T target, Object... paramValues) {
        setText(text);
        this.target = target;

        EnabledIf enabledIf = method.getAnnotation(EnabledIf.class);
        this.enabledIfExpr = (enabledIf != null) ? enabledIf.value() : null;

        Confirm confirm = method.getAnnotation(Confirm.class);
        OnSuccess onSuccess = method.getAnnotation(OnSuccess.class);

        setOnAction(event -> {
            // action was triggered
            if (confirm != null) {
                // we must confirm before executing the method: eval message and display alert
                String confirmMessage = (String) SpelUtil.eval(confirm.value(), this.getTarget());
                Alert alert = new Alert(confirm.alertType(), confirmMessage, ButtonType.OK, ButtonType.CANCEL);
                alert.setTitle(text);
                alert.initOwner(getParentPopup().getOwnerWindow());
                Optional<ButtonType> choice = alert.showAndWait();
                if (choice.orElse(ButtonType.CANCEL) != ButtonType.OK) {
                    return;
                }
            }
            try {
                // invoke method with specified parameter values
                log.debug("Invoking {} on {} with parameters {}", method, this.getTarget(), paramValues);
                Object result = method.invoke(this.getTarget(), paramValues);
                log.debug("Method invocation returned: {}", result);

                if (onSuccess != null) {
                    // we need to do something with the result
                    final String resultVariableName = onSuccess.resultVariableName();
                    Map<String, Object> contextVariables = Maps.mapOf(resultVariableName, result);
                    String expr = onSuccess.value();
                    SpelUtil.eval(expr, evalContextRoot, contextVariables);
                }

            } catch (Exception e) {
                log.error("Exception while invoking method", e);
            }
        });
    }

    @Override
    public void contextualize(T target) {
        setTarget(target);

        if (enabledIfExpr != null) {
            Boolean enabled = (Boolean) SpelUtil.eval(enabledIfExpr, target);
            setVisible(enabled);
        }

    }

}
