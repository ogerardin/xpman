package com.ogerardin.xplane.events;

public interface EventSource<E> {
    boolean registerListener(EventListener<E> listener);
}
