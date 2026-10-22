package com.ogerardin.xplane.util;

import lombok.experimental.UtilityClass;

import java.util.stream.Stream;

/**
 * Stream utilities for concatenating multiple streams of the same element type.
 */
@UtilityClass
public class Streams {

    @SafeVarargs
    public <T> Stream<T> concat(Stream<T>... streams) {
        return Stream.of(streams).flatMap(s -> s);
    }
}
