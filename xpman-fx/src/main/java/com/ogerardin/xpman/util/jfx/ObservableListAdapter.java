package com.ogerardin.xpman.util.jfx;

import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * An adapter that converts a {@link List} that is also an {@link Observable} into a {@link ObservableList}.
 * Any update on the source List triggers a global replace change.
 */
@Slf4j
public class ObservableListAdapter<T> extends ObservableListBase<T> implements ObservableList<T>, Observer {

    private final List<T> list;

    public ObservableListAdapter(List<T> list) {
        if (! (list instanceof Observable)) {
            throw new IllegalArgumentException("argument does not implement Observable");
        }
        this.list = list;
        ((Observable) list).addObserver(this);
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public void update(Observable o, Object arg) {
        beginChange();
        nextReplace(0, size(), list);
        endChange();
    }
}
