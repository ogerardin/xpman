package com.ogerardin.xplane.config.scenery;

import lombok.Data;
import lombok.NonNull;

import java.nio.file.Path;

@Data
public class SceneryPackage {

    @NonNull
    final Path folder;

    public String getName() {
        return folder.getFileName().toString();
    }

}
