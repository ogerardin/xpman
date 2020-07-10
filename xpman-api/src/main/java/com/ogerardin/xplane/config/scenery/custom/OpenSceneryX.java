package com.ogerardin.xplane.config.scenery.custom;

import com.ogerardin.xplane.config.scenery.SceneryPackage;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class OpenSceneryX extends SceneryPackage {

    public OpenSceneryX(@NonNull Path folder) throws InstantiationException {
        super(folder);
        IntrospectionHelper.require(getFolder().getFileName().toString().equals("OpenSceneryX"));
    }

    @SneakyThrows
    public String getVersion() {
        final Path versionFile = getFolder().resolve("version.txt");
        final String version = Files.readAllLines(versionFile).get(0);
        return version;
    }
}
