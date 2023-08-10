package com.ogerardin.xplane.inspection;

import lombok.NonNull;

/**
 * An object that produces a list of {@link InspectionMessage}s when applied to a target
 * @param <T> the type of the target.
 */
@FunctionalInterface
public interface Inspection<T> {

    InspectionResult inspect(@NonNull T target);

    static <X> Inspection<X> empty() {
        return target -> InspectionResult.empty();
    }

    default Inspection<T> and(Inspection<T> other) {
        return target -> {
            var result = this.inspect(target);
            return result.isNotAbort() ? result.append(other.inspect(target)) : result;
        };
    }

    default Inspectable inspectable(T target) {
        return () -> this.inspect(target);
    }

}
