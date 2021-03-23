package com.ogerardin.xplane.events;

public interface EventListener<E> {
    void onEvent(E event);
}
