package com.ogerardin.xplane.util.events;

import java.util.HashSet;
import java.util.Set;

/**
 * Lightweight event dispatcher that maintains a set of listeners and broadcasts events to them synchronously.
 */
public class EventDispatcher<E> implements EventSource<E> {

    private final Set<EventListener<E>> listeners = new HashSet<>();

    @Override
    public boolean registerListener(EventListener<E> listener) {
        return listeners.add(listener);
    }

    public void fireEvent(E event) {
        listeners.forEach(listener -> listener.onEvent(event));
    }

}
