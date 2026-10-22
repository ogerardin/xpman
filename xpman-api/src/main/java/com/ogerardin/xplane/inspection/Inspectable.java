package com.ogerardin.xplane.inspection;

/**
 * A domain object that can be inspected for warnings, errors, or abort conditions.
 *
 * <p>The inspection framework is the project's composable precondition/postcondition
 * mechanism: each {@link Inspection} produces an {@link InspectionResult}, and inspections
 * can be chained via {@link #and(Inspectable)} — the chain short-circuits when any result
 * contains an aborting message (set by {@code Assert*} inspections).</p>
 */
@FunctionalInterface
public interface Inspectable {

    InspectionResult inspect();

    default Inspectable and(Inspectable other) {
        return () -> {
            var result = Inspectable.this.inspect();
            return result.isNotAbort() ? result.append(other.inspect()) : result;
        };
    }

}
