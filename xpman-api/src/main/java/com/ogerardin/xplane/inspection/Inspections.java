package com.ogerardin.xplane.inspection;

import com.ogerardin.xplane.config.XPlaneInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public List<InspectionMessage> apply(T target, XPlaneInstance xPlaneInstance) {
        return inspections.stream()
                .map(inspection -> inspection.apply(target, xPlaneInstance))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
