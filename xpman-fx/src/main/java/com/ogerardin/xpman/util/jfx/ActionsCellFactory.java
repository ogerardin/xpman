package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xpman.util.jfx.panels.menu.Contextualizable;
import com.ogerardin.xpman.util.jfx.panels.menu.MethodButton;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 */
@Slf4j
@Data
@AllArgsConstructor
public class ActionsCellFactory<S, T> implements TableCellFactory<S, T> {

    protected final Object evaluationContextRoot;

    @Getter(AccessLevel.NONE)
    private final Map<T, ButtonBar> MENU_CACHE = new HashMap<>();

    @SuppressWarnings("unused")
    public ActionsCellFactory() {
        this(null);
    }

    @Override
    public TableCell<S, T> call(TableColumn<S, T> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(T value, boolean empty) {
                super.updateItem(value, empty);
                setText(null);
                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    ButtonBar buttonBar = getButtonBar(value);
                    setGraphic(buttonBar);
                }
            }
        };
    }

    private ButtonBar getButtonBar(T item) {
        return MENU_CACHE.computeIfAbsent(item, t -> {
            Button[] buttons = buildButtons(t, t.getClass());
            ButtonBar buttonBar = new ButtonBar();
            buttonBar.getButtons().setAll(buttons);
            contexualize(buttonBar, t);
            return buttonBar;
        });
    }

    protected Button[] buildButtons(T target, Class<?> targetClass) {
        return IntrospectionHelper.computeRelevantMethods(targetClass).stream()
                .map(method -> buildButton(method, target))
                .toArray(Button[]::new);
    }

    private Button buildButton(Method method, T target) {
        String label = IntrospectionHelper.getLabelForMethod(method);
        return new MethodButton<>(method, label, evaluationContextRoot, target);
    }

    @Synchronized
    protected void contexualize(ButtonBar buttonBar, T target) {
        if (buttonBar == null) {
            return;
        }
        log.debug("Contextualizing button bar for {}", target);
        buttonBar.getButtons().stream()
                .filter(Contextualizable.class::isInstance)
                .map(item -> (Contextualizable<T>) item)
                .forEach(contextualizable -> contextualizable.contextualize(target));
    }

}
