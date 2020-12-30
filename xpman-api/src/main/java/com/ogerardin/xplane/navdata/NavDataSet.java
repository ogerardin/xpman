package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.XPlaneInstance;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import lombok.Data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public abstract class NavDataSet implements InspectionsProvider<NavDataSet>, NavDataItem {

    private final String name;

    private final Path folder;

    private List<NavDataFile> files = new ArrayList<>();

    @Override
    public Inspections<NavDataSet> getInspections(XPlaneInstance xPlaneInstance) {
        return null;
    }

    @Override
    public List<? extends NavDataItem> getChildren() {
        return files;
    }

    @Override
    public Boolean getExists() {
        return files.stream().map(NavDataFile::getFile).anyMatch(Files::exists);
    }
}
