package com.ogerardin.xplane;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import com.ogerardin.xplane.install.InstallTarget;
import lombok.Data;

import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.stream.Collectors;

@Data
public abstract class Manager<T extends InspectionsProvider<T>> implements Inspection<T>, EventListener {

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
