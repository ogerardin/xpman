package com.ogerardin.xpman.util.jfx.panels.menu;

/**
 * Indicates that this class can be contextualized (customized for a specific context) by invoking {@link #contextualize}.
 * @param <T> type of the context
 */
public interface Contextualizable<T> {

    /** Contextualize this object with the specified context */
    void contextualize(T context);
}
