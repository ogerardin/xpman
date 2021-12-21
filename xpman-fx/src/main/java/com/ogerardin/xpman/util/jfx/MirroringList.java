package com.ogerardin.xpman.util.jfx;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import lombok.experimental.Delegate;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A List that mirrors a base List by applying a converter on all items.
 * @param <E> item type of the target List
 * @param <F> item type of the base list
 */
public class MirroringList<E, F> extends TransformationList<E, F> implements ObservableList<E> {

    /** mapping function from base list item type to target list item type */
    private final Function<F, E> converter;

    public MirroringList(ObservableList<? extends F> list, Function<F, E> converter) {
        super(list);
        this.converter = converter;
    }

    @Override
    public int getSourceIndex(int index) {
        return index ;
    }

    @Override
    public E get(int index) {
        return converter.apply(getSource().get(index));
    }

    @Override
    public int size() {
        return getSource().size();
    }

    @Override
    protected void sourceChanged(Change<? extends F> change) {
        fireChange(new DelegatingChange(change, this));
    }


    /**
     * An implementation of {@link Change} that delegates all methods to a specified change except {@link #getRemoved()}
     */
    private class DelegatingChange extends Change<E> implements DelegatingChangeExcluded<E> {

        @Delegate(excludes = DelegatingChangeExcluded.class)
        private final Change<? extends F> change;

        public DelegatingChange(Change<? extends F> change, MirroringList<E, F> list) {
            super(list);
            this.change = change;
        }

        @Override
        protected int[] getPermutation() {
            return new int[0];
        }

        @Override
        public List<E> getRemoved() {
            return change.getRemoved().stream()
                    .map(converter)
                    .collect(Collectors.toList());
        }
    }

    /**
     * This interface is only used to exclude some methods from delegated methods via Lombok's @{@link Delegate}
     * so that the compiler doesn't complain.
     */
    @SuppressWarnings("unused")
    private interface DelegatingChangeExcluded<E> {
        List<E> getRemoved();
        ObservableList<E> getList();
        List<E> getAddedSubList();
    }

}
