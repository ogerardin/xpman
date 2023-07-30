package com.ogerardin.xpman.util.jfx.menu;

import com.ogerardin.xpman.util.jfx.cell_factory.TableCellFactory;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link TableCellFactory} that creates a cell containing a set of actions computed by introspecting
 * the value type T. The type of control used for the action is determined by {@link #style}.
 */
@Slf4j
@Data
@RequiredArgsConstructor
public class ActionsCellFactory<S, T> implements TableCellFactory<S, T> {

    public enum Style {
        /** Style where each action is represented by a {@link javafx.scene.control.Button} */
        BUTTON {
            @Override
            <T> Control buildControl(Method method, String text, Object evalContextRoot, T target) {
                return new MethodButton<>(text, method, evalContextRoot, target);
            }
        },
        /** Style where each action is represented by {@link javafx.scene.control.Hyperlink} */
        HYPERLINK {
            @Override
            <T> Control buildControl(Method method, String text, Object evalContextRoot, T target) {
                return new MethodHyperlink<>(text, method, evalContextRoot, target);
            }
        };

        abstract <T> Control buildControl(Method method, String text, Object evalContextRoot, T target);
    }

    protected final Object evaluationContextRoot;

    protected Style style = Style.BUTTON;

    @Getter(AccessLevel.NONE)
    private final Map<T, Pane> MENU_CACHE = new HashMap<>();

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
                    Node actions = getActionsComponent(value);
                    setGraphic(actions);
                }
            }
        };
    }

    private Pane getActionsComponent(T item) {
        Pane pane = MENU_CACHE.computeIfAbsent(item, t -> {
            Control[] buttons = buildActionControls(t, t.getClass(), style);
            HBox container = new HBox();
            container.getChildren().setAll(buttons);
            return container;
        });
        contextualize(pane);
        return pane;
    }

    protected Control[] buildActionControls(T target, Class<?> targetClass, Style style) {
        return IntrospectionHelper.computeRelevantMethods(targetClass).stream()
                .map(method -> buildControl(method, target, style))
                .toArray(Control[]::new);
    }

    private Control buildControl(Method method, T target, Style style) {
        String label = IntrospectionHelper.getLabelForMethod(method);
        Control control = style.buildControl(method, label, evaluationContextRoot, target);
        // make sure the control becomes unmanaged when set to invisible (to force relayout)
        control.managedProperty().bind(control.visibleProperty());
        return control;
    }

    @Synchronized
    protected void contextualize(Pane container) {
        if (container == null) {
            return;
        }
        log.debug("Contextualizing container");
        container.getChildren().stream()
                .filter(Refreshable.class::isInstance)
                .map(Refreshable.class::cast)
                .forEach(Refreshable::refresh);
    }

}
