package com.ogerardin.xplane.navdata;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A single Nav data file in a {@link NavDataSet}
 */
@Data
public class NavDataFile implements NavDataItem {

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final NavDataSet navDataSet;

    private final Path file;

    @Override
    public String getName() {
        final Path relativePath = navDataSet.getXPlane().getBaseFolder().relativize(getFullPath());
        return relativePath.toString();
    }

    @Override
    public Boolean getExists() {
        return Files.exists(getFullPath());
    }

    private Path getFullPath() {
        return navDataSet.getFolder().resolve(file);
    }
}
