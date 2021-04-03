package com.ogerardin.xpman.util.jfx.panels.menu;

import com.ogerardin.xpman.util.SpelUtil;
import com.ogerardin.xpman.util.jfx.TableRowFactory;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import lombok.*;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static javafx.beans.binding.Bindings.when;

/**
 * A row factory that adds a customized context menu built by introspecting the item class and looking for specxific
 * annotations.
 * @param <T> item class
 *
 * @see Label
 * @see EnabledIf
 * @see Confirm
 * @see ForEach
 */
@Data
public class IntrospectingContextMenuRowFactory<T> implements TableRowFactory<T> {

    @NonNull
    private final Class<? extends T> itemClass;

    private final Object evaluationContextRoot;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final List<Method> methods = computeRelevantMethods();

    @Override
    public TableRow<T> call(TableView<T> tableView) {
        TableRow<T> row = new TableRow<>();
        addContextMenu(row);
        return row;
    }

    /**
     * Builds a context menu based on exposed methods of the item class.
     */
    protected void addContextMenu(TableRow<T> row) {

        // build context menu
        TableView<T> tableView = row.getTableView();
        ContextMenu rowMenu = new ContextMenu(buildMenuItems(tableView));

        // only display context menu for non-null items
        row.contextMenuProperty().bind(
                when(Bindings.isNotNull(row.itemProperty()))
                        .then(rowMenu)
                        .otherwise((ContextMenu) null));

        // before displaying context menu, cutomize it for actual row item
        row.setOnContextMenuRequested(event -> contexualizeMenu(rowMenu, row.getItem()));
    }

    private MenuItem[] buildMenuItems(Object tableView) {
        return getMethods().stream()
                .map(method -> buildMenuItem(method, tableView))
                .toArray(MenuItem[]::new);
    }

    private List<Method> computeRelevantMethods() {
        return Arrays.stream(itemClass.getDeclaredMethods())
                // skip if method is an Object method
                .filter(this::isNotObjectMethod)
                // skip non public or abstract methods
                .filter(method -> Modifier.isPublic(method.getModifiers()) && ! Modifier.isAbstract(method.getModifiers()))
                // skip setters/getters
                .filter(method -> ! method.getName().startsWith("set") && ! method.getName().startsWith("get") && ! method.getName().startsWith("is"))
                .collect(Collectors.toList());
    }

    private boolean isNotObjectMethod(Method method) {
        try {
            Object.class.getMethod(method.getName(), method.getParameterTypes());
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private MenuItem buildMenuItem(Method method, Object evalContextRoot) {
        ForEach forEach = method.getAnnotation(ForEach.class);
        if (forEach != null) {
            return new ForEachMenuItem<T>(evalContextRoot, forEach, method);
        }

        //TODO allow the use of @Value on methods that are not annotated with @ForEach
        var label = method.getAnnotation(Label.class);
        String text;
        if (label != null) {
            String expr = label.value();
            text = (String) SpelUtil.eval(expr, null);
        } else {
            // no @Label: try to make up something human-readable from the method name
            String[] words = StringUtils.splitByCharacterTypeCamelCase(method.getName());
            words[0] = StringUtils.capitalize(words[0]);
            text = String.join(" ", words);
        }
        return new MethodMenuItem<>(evaluationContextRoot, text, method, null);
    }

    private void contexualizeMenu(ContextMenu contextMenu, T item) {
        contextMenu.getItems().stream()
                .filter(Contextualizable.class::isInstance)
                .map(menuItem -> (Contextualizable<T>) menuItem)
                .forEach(contextualizable -> contextualizable.contextualize(item));

    }

}
