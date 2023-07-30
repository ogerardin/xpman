package com.ogerardin.xplane.manager;

import lombok.*;

import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
@ToString()
public class ManagerEvent<T> {

    public enum Type {
        LOADING, LOADED;
    }

    @NonNull
    private final Type type;

    private final Manager<T> source;

    @ToString.Exclude
    private final List<T> items;
}
