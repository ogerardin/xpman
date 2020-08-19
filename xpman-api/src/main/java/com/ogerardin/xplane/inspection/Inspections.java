package com.ogerardin.xplane.inspection;

import one.util.streamex.StreamEx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A collection of {@link Inspection}s for a common target type.
 * @param <T> target type of the inspections
 */
public class Inspections<T> implements Inspection<T> {

    private final List<Inspection<T>> inspections = new ArrayList<>();

    @SafeVarargs
    public Inspections(Inspection<T>... inspections) {
        this.inspections.addAll(Arrays.asList(inspections));
    }

    @SafeVarargs
    public static <T> Inspections<T> of(Inspection<T>... inspections) {
        return new Inspections<>(inspections);
    }

    @SafeVarargs
    public final Inspections<T> append(Inspection<T>... inspections) {
        this.inspections.addAll(Arrays.asList(inspections));
        return this;
    }

    public final Stream<Inspection<T>> stream() {
        return inspections.stream();
    }

    @Override
    public List<InspectionMessage> apply(T target) {
        return StreamEx.of(inspections)
                .map(inspection -> inspection.apply(target))
                // short-circuit when one of the inspections return an aborting message
                .takeWhileInclusive(messages -> messages.stream().noneMatch(InspectionMessage::isAbort))
                .toFlatList(Function.identity());
    }
}
