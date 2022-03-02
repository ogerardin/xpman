package com.ogerardin.xpman.util.jfx.panels.menu;

import com.ogerardin.xpman.util.SpelUtil;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for implementing a {@link ContextMenu} that is built dynamically by introspecting a target class.
 * The menu can be attached to any Node using {@link javafx.scene.Node#setOnContextMenuRequested} and contextualized
 * using {@link #contexualizeMenu}
 *
 * @see MethodMenuItem
 * @param <T>
 */
@Data
public abstract class IntrospectingContextMenu<T> {
    @NonNull
    private final Class<? extends T> itemClass;

    private final Object evaluationContextRoot;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final List<Method> methods = computeRelevantMethods();

    protected MenuItem[] buildMenuItems(Object tableView) {
        return getMethods().stream()
                .map(method -> buildMenuItem(method, tableView))
                .toArray(MenuItem[]::new);
    }

    private List<Method> computeRelevantMethods() {
        return Arrays.stream(itemClass.getDeclaredMethods())
                // skip if method is an Object method
                .filter(this::isNotObjectMethod)
                // skip non public or abstract methods
                .filter(method -> Modifier.isPublic(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers()))
                // skip setters/getters
                .filter(method -> !method.getName().startsWith("set") && !method.getName().startsWith("get") && !method.getName().startsWith("is"))
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

    /** Customize the menu for the current row item by calling {@link Contextualizable#contextualize(Object)} on
     * all {@link MenuItem}s that implement {@link Contextualizable}, passing it the current orw item.
     */
    protected void contexualizeMenu(ContextMenu contextMenu, T item) {
        contextMenu.getItems().stream()
                .filter(Contextualizable.class::isInstance)
                .map(menuItem -> (Contextualizable<T>) menuItem)
                .forEach(contextualizable -> contextualizable.contextualize(item));

    }

}
