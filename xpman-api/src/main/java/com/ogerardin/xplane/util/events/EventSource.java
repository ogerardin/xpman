package com.ogerardin.xplane.util.events;

public interface EventSource<E> {
    boolean registerListener(EventListener<E> listener);
}
