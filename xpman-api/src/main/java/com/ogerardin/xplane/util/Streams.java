package com.ogerardin.xplane.util;

import lombok.experimental.UtilityClass;

import java.util.stream.Stream;

@UtilityClass
public class Streams {

    @SafeVarargs
    public <T> Stream<T> concat(Stream<T>... streams) {
        return Stream.of(streams).flatMap(s -> s);
    }
}
