package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import lombok.Data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A nav data folder containing a set of {@link NavDataFile}s
 */
@Data
public abstract class NavDataSet implements InspectionsProvider<NavDataSet>, NavDataItem {

    private final String name;

    private final XPlane xPlane;

    private final Path folder;

    private List<NavDataFile> files = new ArrayList<>();

    public NavDataSet(String name, XPlane xPlane, Path folder, String... fileNames) {
        this.name = name;
        this.xPlane = xPlane;
        this.folder = folder;
        List<NavDataFile> files = Arrays.stream(fileNames)
//                .map(folder::resolve)
                .map(Paths::get)
                .map((Path file) -> new NavDataFile(this, file))
                .collect(Collectors.toList());
        setFiles(files);
    }


    @Override
    public Inspections<NavDataSet> getInspections(XPlane xPlane) {
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
