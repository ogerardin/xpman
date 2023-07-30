package com.ogerardin.xplane.util.events;

public interface EventListener<E> {
    void onEvent(E event);
}
