package com.ogerardin.xpman.util.jfx.panels.menu;

import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xpman.util.SpelUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
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
        super(text);
        this.target = target;

        EnabledIf enabledIf = method.getAnnotation(EnabledIf.class);
        this.enabledIfExpr = (enabledIf != null) ? enabledIf.value() : null;

        Confirm confirm = method.getAnnotation(Confirm.class);
        OnSuccess onSuccess = method.getAnnotation(OnSuccess.class);

        var builder = new MethodAction.MethodActionBuilder<>()
                .method(method)
                .target(target)
                .paramValues(paramValues);

        if (confirm != null) {
            builder.confirm(() -> this.confirm(getParentPopup().getOwnerWindow(), text, confirm.alertType(), confirm.value()));
        }
        if (onSuccess != null) {
            builder.onSuccess(result -> this.onSuccess(result, onSuccess.resultVariableName(), onSuccess.value(), evalContextRoot));
        }

        MethodAction<Object> methodAction = builder.build();

        setOnAction(event -> methodAction.run());
    }

    private void onSuccess(Object result, String resultVariableName, String onSuccessExpr, Object evalContextRoot) {
        // we need to do something with the result
        Map<String, Object> contextVariables = Maps.mapOf(resultVariableName, result);
        SpelUtil.eval(onSuccessExpr, evalContextRoot, contextVariables);
    }

    private boolean confirm(Window ownerWindow, String title, AlertType alertType, String msgExpression) {
        // we must confirm before executing the method: eval message and display alert
        String confirmMessage = (String) SpelUtil.eval(msgExpression, getTarget());
        Alert alert = new Alert(alertType, confirmMessage, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.initOwner(ownerWindow);
        Optional<ButtonType> choice = alert.showAndWait();
        return choice.orElse(ButtonType.CANCEL) == ButtonType.OK;
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
