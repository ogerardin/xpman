package com.ogerardin.xplane.scenery.custom;

import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;


/**
 * Specialized SceneryPackage class that handles the OpenSceneryX library.<p>
 * See <a href="https://www.opensceneryx.com">www.opensceneryx.com</a>
 */
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

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.merge(super.getLinks(),
                Maps.mapOf("OpenSceneryX project home page", new URL("https://www.opensceneryx.com/"))
        );
    }

    @SneakyThrows
    @Override
    public URL getIconUrl() {
        return new URL("https://raw.githubusercontent.com/OpenSceneryX/Library/develop/icon.png");
    }
}
