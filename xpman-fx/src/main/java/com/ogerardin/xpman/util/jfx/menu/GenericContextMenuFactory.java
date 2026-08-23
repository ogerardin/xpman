package com.ogerardin.xpman.util.jfx.menu;

/**
 * A concrete, general-purpose {@link IntrospectingContextMenuFactory} whose introspected
 * context menus can be attached to any node (cards, tiles, ...) — not just table rows.
 *
 * @param <T> type of the target object
 */
public class GenericContextMenuFactory<T> extends IntrospectingContextMenuFactory<T> {

    public GenericContextMenuFactory(Object evaluationContextRoot) {
        super(evaluationContextRoot);
    }
}
