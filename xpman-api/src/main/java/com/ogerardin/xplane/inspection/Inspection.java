package com.ogerardin.xplane.inspection;

import java.util.List;

/**
 * An object that produces a list of {@link InspectionMessage}s when applied to a target
 * @param <T> the type of the target.
 */
@FunctionalInterface
public interface Inspection<T> {

    List<InspectionMessage> inspect(T target);
}
