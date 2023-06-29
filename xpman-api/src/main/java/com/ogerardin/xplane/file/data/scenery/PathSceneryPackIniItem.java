package com.ogerardin.xplane.file.data.scenery;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = false)
@Data
public non-sealed class PathSceneryPackIniItem extends SceneryPackIniItem {
    private final Path folder;
}
