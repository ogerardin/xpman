package com.ogerardin.xplane.inspection;

import lombok.Data;
import lombok.experimental.Delegate;
import one.util.streamex.StreamEx;

import java.util.Collections;
import java.util.List;

/**
 * An immutable container of {@link InspectionMessage}s produced by running an
 * {@link com.ogerardin.xplane.inspection.Inspection} against an {@link Inspectable} object.
 *
 * <p>Delegates {@link List} methods to its internal message list and provides
 * {@link #append(InspectionResult)} to merge results and {@link #isNotAbort()} to
 * check whether any message sets the abort flag (used by chained inspections to
 * short-circuit).</p>
 */
@Data
public class InspectionResult {

    @Delegate
    private final List<InspectionMessage> messages;

    private InspectionResult(List<InspectionMessage> messages) {
        this.messages = Collections.unmodifiableList(messages);
    }

    public static InspectionResult empty() {
        return InspectionResult.of(Collections.emptyList());
    }

    public static InspectionResult of(InspectionMessage message) {
        return InspectionResult.of(Collections.singletonList(message));
    }

    public static InspectionResult of(List<InspectionMessage> messages) {
        return new InspectionResult(messages);
    }

    /**
     * Returns true if at least one of the messages is aborting
     */
    public boolean isNotAbort() {
        return messages.stream().noneMatch(InspectionMessage::isAbort);
    }

    public InspectionResult append(InspectionResult other) {
        return new InspectionResult(StreamEx.of(messages, other.messages).flatMap(List::stream).toList());
    }

}
