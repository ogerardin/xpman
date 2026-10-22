package com.ogerardin.xpman.util.jfx.menu;

import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xpman.util.SpelUtil;
import com.ogerardin.xpman.util.jfx.menu.annotation.Confirm;
import com.ogerardin.xpman.util.jfx.menu.annotation.OnSuccess;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Configures a {@link MethodAction} by introspecting a controller method and wiring up
 * confirmation dialogs and post-execution callbacks based on its annotations.
 * <p>
 * Reads {@link com.ogerardin.xpman.util.jfx.menu.annotation.Confirm @Confirm} to display a confirmation
 * alert before invoking the method (with SpEL-evaluated message), and {@link com.ogerardin.xpman.util.jfx.menu.annotation.OnSuccess @OnSuccess}
 * to execute a SpEL expression after successful completion (with the method's return value bound to a
 * named variable in the evaluation context). Destructive operations containing "uninstall" in the message
 * get a danger-styled OK button.
 * <p>
 * Reflection-heavy: caches the constructed action via lazy initialization, but the method, target object,
 * parameter values, and evaluation context root are all held as final fields.
 *
 * @param <T> the type of the target object whose method will be invoked
 * @param <R> the return type of the method
 */
@Slf4j
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

        var builder = new MethodAction.MethodActionBuilder<T, R>()
                .method(method)
                .target(target)
                .paramValues(paramValues)
                .windowSupplier(windowSupplier);

        Confirm confirm = method.getAnnotation(Confirm.class);
        if (confirm != null) {
            // we must confirm before executing the method: eval message and display alert
            builder.confirm(() -> this.confirm(windowSupplier.get(), confirm.alertType(), confirm.value()));
        }
        OnSuccess onSuccess = method.getAnnotation(OnSuccess.class);
        if (onSuccess != null) {
            // we must do something upton completion
            builder.onSuccess(result -> {
                this.onSuccess(result, onSuccess.resultVariableName(), onSuccess.value(), evalContextRoot);
                log.info("{}: done", IntrospectionHelper.getLabelForMethod(method));
            });
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
        // destructive confirmations (uninstalls etc.) get a danger accent on the OK button
        if (confirmMessage.toLowerCase().contains("uninstall")) {
            alert.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add("danger-button");
        }
        Optional<ButtonType> choice = alert.showAndWait();
        return choice.orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

}
