package com.ogerardin.xplane;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Manager<T extends InspectionsProvider<T>> implements Inspection<T> {

    protected final XPlane xPlane;

    public List<InspectionMessage> inspect(T target) {
        Inspections<T> inspections = target.getInspections(xPlane);
        final List<InspectionMessage> inspectionMessages = inspections.stream()
                .map(inspection -> inspection.inspect(target))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return inspectionMessages;
    }
}
