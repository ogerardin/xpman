package com.ogerardin.xplane;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

public abstract class ManagerEvent<T> {

    public static class Loading<T> extends ManagerEvent<T> {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Loaded<T> extends ManagerEvent<T> {
        private final List<T> items;
    }
}
