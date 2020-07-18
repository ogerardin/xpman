package com.ogerardin.xplane.config;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Manager<T extends InspectionsProvider<T>> {

    protected final XPlaneInstance xPlaneInstance;

    public List<InspectionMessage> inspect(T target) {
        Inspections<T> inspections = target.getInspections();
        final List<InspectionMessage> inspectionMessages = inspections.stream()
                .map(inspection -> inspection.apply(target, xPlaneInstance))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return inspectionMessages;
    }
}
