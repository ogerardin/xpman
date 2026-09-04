package com.ogerardin.xplane.navdata;

import lombok.Data;
import lombok.ToString;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An airspace.txt file (XP12) as part of a {@link NavDataSet}.
 *
 * <p>airspace.txt contains the boundaries of special-use airspaces. Its content
 * is not parsed.</p>
 */
@Data
public class AirspaceFile implements NavDataItem {

    @ToString.Exclude
    private final NavDataSet navDataSet;

    private final Path file;

    @Override
    public Path getPath() {
        return file;
    }

    @Override
    public Boolean getExists() {
        return Files.exists(file);
    }

    @Override
    public String getName() {
        return navDataSet.getXPlane().getBaseFolder().relativize(file).toString();
    }
}