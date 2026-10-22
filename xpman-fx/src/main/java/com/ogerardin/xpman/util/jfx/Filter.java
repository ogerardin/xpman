package com.ogerardin.xpman.util.jfx;

import lombok.Data;

import java.util.function.Predicate;

/** Named predicate wrapper for filtering UI collections, with a display-friendly {@code toString}. */
@Data
public class Filter<T> {
    private final String name;
    private final Predicate<T> predicate;

    @Override
    public String toString() {
        return name;
    }

    public static <T> Filter<T> all() {
        return new Filter<>("All", o -> true);
    }
}
