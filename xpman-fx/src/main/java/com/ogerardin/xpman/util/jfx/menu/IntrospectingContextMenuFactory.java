package com.ogerardin.xpman.util.jfx.menu;

import com.ogerardin.xpman.util.jfx.menu.annotation.ForEach;
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
        ContextMenu menu = MENU_CACHE.computeIfAbsent(item, t -> {
            MenuItem[] menuItems = buildMenuItems(t, t.getClass());
            return new ContextMenu(menuItems);
        });
        contextualize(menu);
        return menu;
    }


    protected MenuItem[] buildMenuItems(T target, Class<?> aClass) {
        return IntrospectionHelper.computeRelevantMethods(aClass).stream()
                .map(method -> buildMenuItem(method, target))
                .toArray(MenuItem[]::new);
    }

    private MenuItem buildMenuItem(Method method, T target) {
        ForEach forEach = method.getAnnotation(ForEach.class);
        if (forEach != null) {
            return new ForEachMenuItem<T>(evaluationContextRoot, forEach, method, target);
        }

        String label = IntrospectionHelper.getLabelForMethod(method);
        //TODO allow the use of @Value on methods that are not annotated with @ForEach
        return new MethodMenuItem<>(evaluationContextRoot, label, method, target);
    }


    @Synchronized
    protected void contextualize(ContextMenu contextMenu) {
        if (contextMenu == null) {
            return;
        }
        log.debug("Contextualizing menu");
        contextMenu.getItems().stream()
                .filter(Refreshable.class::isInstance)
                .map(Refreshable.class::cast)
                .forEach(Refreshable::refresh);
    }

}
