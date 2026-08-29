package com.ogerardin.xplane.file.data.scenery;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public non-sealed class PathSceneryPackIniItem extends SceneryPackIniItem {

    private final Path folder;

    public PathSceneryPackIniItem(Path folder) {
        this(folder, false);
    }

    public PathSceneryPackIniItem(Path folder, boolean disabled) {
        super(disabled);
        this.folder = folder;
    }

    @Override
    public Path resolveFolder(Path baseFolder, Path globalAirportsFolder) {
        return baseFolder.resolve(folder);
    }
}
