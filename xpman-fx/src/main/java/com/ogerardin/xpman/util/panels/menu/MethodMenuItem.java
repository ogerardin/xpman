package com.ogerardin.xpman.util.panels.menu;

import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xpman.util.SpelUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

@Slf4j
public class MethodMenuItem<T> extends MenuItem implements Contextualizable<T> {

    private final String enabledIfExpr;
    private final String confirmExpr;

    @Setter @Getter
    private T target;

    public <C> MethodMenuItem(C controller, String text, Method method, T target, Object... paramValues) {
        setText(text);
        this.target = target;

        EnabledIf enabledIf = method.getAnnotation(EnabledIf.class);
        this.enabledIfExpr = (enabledIf != null) ? enabledIf.value() : null;

        Confirm confirm = method.getAnnotation(Confirm.class);
        this.confirmExpr = (confirm != null) ? confirm.value() : null;

        OnSuccess onSuccess = method.getAnnotation(OnSuccess.class);

        setOnAction(event -> {
            if (confirm != null) {
                // we must confirm before executing the method: eval message and display alert
                String confirmMessage = (String) SpelUtil.eval(confirmExpr, this.getTarget());
                Alert alert = new Alert(CONFIRMATION, confirmMessage);
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
                    SpelUtil.eval(expr, controller, contextVariables);
                }

            } catch (Exception e) {
                log.error("Exception while invoking method", e);
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
