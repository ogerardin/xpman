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

/**
 * Base class for factories that create {@link ContextMenu}s by introspecting an object.
 * Warning: for efficiency reasons, the menu is cached and reused for each object; make sure that the #hashCode() and
 * #equals() methods of the object are properly implemented.
 * Call {@link #clearCache()} whenever the underlying items are (re)loaded, so that menus for stale
 * objects don't accumulate (the cached menus strongly reference their targets).
 * @param <T> type of the target object
 */
@Slf4j
@Data
public abstract class IntrospectingContextMenuFactory<T> {

    @Getter(AccessLevel.NONE)
    private final Map<T, ContextMenu> MENU_CACHE = new HashMap<>();

    /**
     * Evicts all cached menus. Call when the item set is (re)loaded.
     */
    public void clearCache() {
        MENU_CACHE.clear();
    }

    private final Object evaluationContextRoot;

    protected ContextMenu getContextMenu(T item) {
        ContextMenu menu = MENU_CACHE.computeIfAbsent(item, t -> {
            MenuItem[] menuItems = buildMenuItems(t, t.getClass());
            return new ContextMenu(menuItems);
        });
        contextualize(menu);
        return menu;
    }

    /**
     * Returns the introspected context menu for the given item; can be attached to any node.
     */
    public ContextMenu menuFor(T item) {
        return getContextMenu(item);
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
