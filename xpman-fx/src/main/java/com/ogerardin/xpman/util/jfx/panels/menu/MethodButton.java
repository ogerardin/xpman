package com.ogerardin.xpman.util.jfx.panels.menu;

import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xpman.util.SpelUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

/**
 */
@Slf4j
public class MethodButton<T> extends Button implements Contextualizable<T> {

    private final String enabledIfExpr;

    @Setter @Getter
    private T target;

    public MethodButton(Method method, String text, Object evalContextRoot, T target, Object... paramValues) {
        super(text);
        this.target = target;

        EnabledIf enabledIf = method.getAnnotation(EnabledIf.class);
        this.enabledIfExpr = (enabledIf != null) ? enabledIf.value() : null;

        var builder = new MethodAction.MethodActionBuilder<>()
                .method(method)
                .target(target)
                .paramValues(paramValues);

        Confirm confirm = method.getAnnotation(Confirm.class);
        if (confirm != null) {
            // we must confirm before executing the method: eval message and display alert
            builder.confirm(() -> this.confirm(getScene().getWindow(), text, confirm.alertType(), confirm.value()));
        }
        OnSuccess onSuccess = method.getAnnotation(OnSuccess.class);
        if (onSuccess != null) {
            // we must do something upton completion
            builder.onSuccess(result -> this.onSuccess(result, onSuccess.resultVariableName(), onSuccess.value(), evalContextRoot));
        }

        MethodAction<Object> methodAction = builder.build();

        setOnAction(event -> methodAction.run());
    }

    private void onSuccess(Object result, String resultVariableName, String onSuccessExpr, Object evalContextRoot) {
        Map<String, Object> contextVariables = Maps.mapOf(resultVariableName, result);
        SpelUtil.eval(onSuccessExpr, evalContextRoot, contextVariables);
    }

    private boolean confirm(Window ownerWindow, String title, AlertType alertType, String msgExpression) {
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
