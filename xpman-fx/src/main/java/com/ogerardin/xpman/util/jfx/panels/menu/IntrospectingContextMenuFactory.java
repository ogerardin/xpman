package com.ogerardin.xpman.util.jfx.panels.menu;

import com.ogerardin.xpman.util.jfx.IntrospectionHelper;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
public abstract class IntrospectingContextMenuFactory<T> {

    @Getter(AccessLevel.NONE)
    private final Map<T, ContextMenu> MENU_CACHE = new HashMap<>();

    private final Object evaluationContextRoot;

    protected ContextMenu getContextMenu(T item) {
        return MENU_CACHE.computeIfAbsent(item, t -> {
            MenuItem[] menuItems = buildMenuItems(item.getClass());
            ContextMenu contextMenu = new ContextMenu(menuItems);
            contexualize(contextMenu, item);
            return contextMenu;
        });
    }


    protected MenuItem[] buildMenuItems(Class<?> aClass) {
        return IntrospectionHelper.computeRelevantMethods(aClass).stream()
                .map(this::buildMenuItem)
                .toArray(MenuItem[]::new);
    }

    private MenuItem buildMenuItem(Method method) {
        ForEach forEach = method.getAnnotation(ForEach.class);
        if (forEach != null) {
            return new ForEachMenuItem<T>(evaluationContextRoot, forEach, method);
        }

        String label = IntrospectionHelper.getLabelForMethod(method);
        //TODO allow the use of @Value on methods that are not annotated with @ForEach
        return new MethodMenuItem<>(evaluationContextRoot, label, method, null);
    }


    /**
     * Customize the menu for the current row item by calling {@link Contextualizable#contextualize(Object)} on
     * all {@link MenuItem}s that implement {@link Contextualizable}, passing it the current orw item.
     */
    @Synchronized
    protected void contexualize(ContextMenu contextMenu, T target) {
        if (contextMenu == null) {
            return;
        }
        log.debug("Contextualizing menu for {}", target);
        contextMenu.getItems().stream()
                .filter(Contextualizable.class::isInstance)
                .map(item -> (Contextualizable<T>) item)
                .forEach(contextualizable -> contextualizable.contextualize(target));
    }

}
