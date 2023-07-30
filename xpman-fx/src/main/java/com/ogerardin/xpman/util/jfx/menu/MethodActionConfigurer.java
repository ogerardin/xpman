package com.ogerardin.xpman.util.jfx.menu;

import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xpman.util.SpelUtil;
import com.ogerardin.xpman.util.jfx.menu.annotation.Confirm;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import com.ogerardin.xpman.util.jfx.menu.annotation.OnSuccess;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Data
public class MethodActionConfigurer<T, R> {

    private final Supplier<Window> windowSupplier;
    private final @NonNull Method method;
    private final Object evalContextRoot;
    private final T target;
    private final Object[] paramValues;

    @Getter(lazy = true)
    private final MethodAction<T, R> methodAction = buildMethodAction();

    private MethodAction<T, R> buildMethodAction() {

        EnabledIf enabledIf = method.getAnnotation(EnabledIf.class);
        String enabledIfExpr = (enabledIf != null) ? enabledIf.value() : null;

        var builder = new MethodAction.MethodActionBuilder<T, R>()
                .method(method)
                .target(target)
                .paramValues(paramValues);

        Confirm confirm = method.getAnnotation(Confirm.class);
        if (confirm != null) {
            // we must confirm before executing the method: eval message and display alert
            builder.confirm(() -> this.confirm(windowSupplier.get(), confirm.alertType(), confirm.value()));
        }
        OnSuccess onSuccess = method.getAnnotation(OnSuccess.class);
        if (onSuccess != null) {
            // we must do something upton completion
            builder.onSuccess(result -> this.onSuccess(result, onSuccess.resultVariableName(), onSuccess.value(), evalContextRoot));
        }

        return builder.build();
    }

    private void onSuccess(Object result, String resultVariableName, String onSuccessExpr, Object evalContextRoot) {
        Map<String, Object> contextVariables = Maps.mapOf(resultVariableName, result);
        SpelUtil.eval(onSuccessExpr, evalContextRoot, contextVariables);
    }

    private boolean confirm(Window ownerWindow, Alert.AlertType alertType, String msgExpression) {
        String confirmMessage = (String) SpelUtil.eval(msgExpression, getTarget());
        Alert alert = new Alert(alertType, confirmMessage, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirm");
        alert.initOwner(ownerWindow);
        Optional<ButtonType> choice = alert.showAndWait();
        return choice.orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

}
