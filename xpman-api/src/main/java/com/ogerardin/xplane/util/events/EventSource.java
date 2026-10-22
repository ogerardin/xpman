package com.ogerardin.xplane.util.events;

/**
 * Source of events that allows registering {@link EventListener} callbacks.
 */
public interface EventSource<E> {
    boolean registerListener(EventListener<E> listener);
}
