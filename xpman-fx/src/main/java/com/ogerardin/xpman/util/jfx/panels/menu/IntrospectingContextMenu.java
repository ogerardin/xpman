package com.ogerardin.xpman.util.jfx.panels.menu;

import com.ogerardin.xpman.util.SpelUtil;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class IntrospectingContextMenu<T> {

    @Getter(AccessLevel.NONE)
    private final Map<T, ContextMenu> MENU_CACHE = new HashMap<>();

    private final Object evaluationContextRoot;

    protected ContextMenu getContextMenu(T item) {
        return MENU_CACHE.computeIfAbsent(item, t -> {
            MenuItem[] menuItems = buildMenuItems(item.getClass());
            ContextMenu contextMenu = new ContextMenu(menuItems);
            contexualizeMenu(contextMenu, item);
            return contextMenu;
        });
    }

    protected MenuItem[] buildMenuItems(Class<?> aClass) {
        return computeRelevantMethods(aClass).stream()
                .map(this::buildMenuItem)
                .toArray(MenuItem[]::new);
    }

    private List<Method> computeRelevantMethods(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredMethods())
                // skip if method is an Object method
                .filter(this::isNotObjectMethod)
                // skip non public or abstract methods
                .filter(method -> Modifier.isPublic(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers()))
                // skip setters/getters
                .filter(method -> !method.getName().startsWith("set") && !method.getName().startsWith("get") && !method.getName().startsWith("is"))
                .toList();
    }

    private boolean isNotObjectMethod(Method method) {
        try {
            Object.class.getMethod(method.getName(), method.getParameterTypes());
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private MenuItem buildMenuItem(Method method) {
        ForEach forEach = method.getAnnotation(ForEach.class);
        if (forEach != null) {
            return new ForEachMenuItem<T>(evaluationContextRoot, forEach, method);
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

    /**
     * Customize the menu for the current row item by calling {@link Contextualizable#contextualize(Object)} on
     * all {@link MenuItem}s that implement {@link Contextualizable}, passing it the current orw item.
     */
    @Synchronized
    protected void contexualizeMenu(ContextMenu contextMenu, T item) {
        if (contextMenu == null) {
            return;
        }
        log.debug("Contextualizing menu for {}", item);
        contextMenu.getItems().stream()
                .filter(Contextualizable.class::isInstance)
                .map(menuItem -> (Contextualizable<T>) menuItem)
                .forEach(contextualizable -> contextualizable.contextualize(item));

    }
}
