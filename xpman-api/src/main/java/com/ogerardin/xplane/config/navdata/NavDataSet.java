package com.ogerardin.xplane.config.navdata;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;

@Data
public abstract class NavDataSet implements InspectionsProvider<NavDataSet>, NavDataItem {

    private final Path folder;

    private final String name;

    private List<NavDataFile> files;

    @Override
    public Inspections<NavDataSet> getInspections(XPlaneInstance xPlaneInstance) {
        return null;
    }

    @Override
    public List<? extends NavDataItem> getChildren() {
        return files;
    }
}
