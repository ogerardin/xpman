package com.ogerardin.xplane;

import com.ogerardin.xplane.events.EventDispatcher;
import com.ogerardin.xplane.events.EventSource;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import lombok.Data;
import lombok.experimental.Delegate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
public abstract class Manager<T extends InspectionsProvider<T>> implements Inspection<T>, EventSource<ManagerEvent<T>> {

    protected final XPlane xPlane;

    @Delegate
    private final EventDispatcher<ManagerEvent<T>> eventSource = new EventDispatcher<>();

    public List<InspectionMessage> inspect(T target) {
        Inspections<T> inspections = target.getInspections(xPlane);
        final List<InspectionMessage> inspectionMessages = inspections.stream()
                .map(inspection -> inspection.inspect(target))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return inspectionMessages;
    }

}
