package com.ogerardin.xplane.util;

import lombok.experimental.Delegate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Observable;

/**
 * A {@link List} decorator that delegates all methods to the wrapped List and adds
 * @param <T>
 */
public class LegacyObservableList<T> extends Observable implements List<T> {

    @Delegate
    private List<T> list;

    public LegacyObservableList(List<T> list) {
        this.list = list;
    }

    public void set(List<T> list) {
        this.list = list;
        setChanged();
        notifyObservers();
    }

    public List<T> get() {
        return Collections.unmodifiableList(list);
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public boolean equals(Object o) {
        return Objects.equals(list, o);
    }

    @Override
    public int hashCode() {
        return list != null ? list.hashCode() : 0;
    }
}
