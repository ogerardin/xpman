package com.ogerardin.xplane.inspection;

import lombok.Data;
import lombok.NonNull;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An {@link Inspection} that, when applied, produces a single {@link InspectionMessage} provided by a given supplier
 * if a given predicate evaluates to false, otherwise produces an empty message list.
 */
@Data
public class PredicateInspection<T> implements Inspection<T> {

    private final Predicate<T> check;

    private final Supplier<InspectionMessage> messageSupplier;

    @Override
    public InspectionResult inspect(@NonNull T target) {
        if (! check.test(target)) {
            return InspectionResult.of(messageSupplier.get());
        }
        return InspectionResult.empty();
    }
}
