package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneObject;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A nav data folder containing a set of {@link NavDataFile}s
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class NavDataSet extends XPlaneObject implements Inspectable, NavDataItem {

    private final String name;

    @ToString.Exclude
    private final String description;

    private final Path folder;

    @Override
    public Path getPath() {
        return getFolder();
    }

    @EqualsAndHashCode.Exclude
    private List<NavDataFile> files = new ArrayList<>();

    protected NavDataSet(String name, String description, XPlane xPlane, Path folder, String... fileNames) {
        super(xPlane);
        this.name = name;
        this.description = description;
        this.folder = folder;
        this.files = Arrays.stream(fileNames)
//                .map(folder::resolve)
                .map(Paths::get)
                .map((Path file) -> new NavDataFile(this, file))
                .toList();
    }


    @Override
    public InspectionResult inspect() {
        //TODO: check that all files exist
        return InspectionResult.empty();
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
