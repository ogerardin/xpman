package com.ogerardin.xplane.util.events;

/**
 * Functional callback interface for receiving events of type {@code E}.
 */
public interface EventListener<E> {
    void onEvent(E event);
}
