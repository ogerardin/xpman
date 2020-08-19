package com.ogerardin.xplane.inspection;

import java.util.List;

/**
 * An object that produces a list of {@link InspectionMessage}s when applied to a target
 * @param <T> the type of the target.
 */
public interface Inspection<T> {

    List<InspectionMessage> apply(T target);
}
