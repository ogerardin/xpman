package com.ogerardin.xplane.inspection;

import lombok.Data;
import lombok.experimental.Delegate;
import one.util.streamex.StreamEx;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;

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

    public static Collector<? super InspectionResult, Object, Object> collector() {
        return null;
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
