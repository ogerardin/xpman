package com.ogerardin.xpman.util.jfx;

public interface CellUpdater<T> {
    void updateItem(T value, boolean empty);
}
