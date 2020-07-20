package com.ogerardin.xplane.inspection;

import com.ogerardin.xplane.config.XPlaneInstance;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An inspection that returns a single {@link InspectionMessage} provided by a given supplier if a given
 * function evaluates to false.
 */
@Data
public class CheckInspection<T> implements Inspection<T> {

    private final Predicate<T> check;

    private final Supplier<InspectionMessage> messageSupplier;

    @Override
    public List<InspectionMessage> apply(T target) {
        if (! check.test(target)) {
            return Collections.singletonList(messageSupplier.get());
        }
        return Collections.emptyList();
    }
}
