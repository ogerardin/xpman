package com.ogerardin.xplane.scenery.custom;

import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;


/**
 * Specialized SceneryPackage class that handles the OpenSceneryX library.<p>
 * See <a href="https://www.opensceneryx.com">www.opensceneryx.com</a>
 */
@SuppressWarnings("unused")
public class OpenSceneryX extends SceneryPackage {

    public OpenSceneryX(@NonNull Path folder) throws InstantiationException {
        super(folder);
        require(getFolder().getFileName().toString().equals("OpenSceneryX"));
    }

    @SneakyThrows
    @Override
    public String getVersion() {
        final Path versionFile = getFolder().resolve("version.txt");
        return Files.readAllLines(versionFile).get(0);
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.merge(super.getLinks(),
                Maps.mapOf("OpenSceneryX project home page", Urls.url("https://www.opensceneryx.com/"))
        );
    }

    @SneakyThrows
    @Override
    public URL getIconUrl() {
        return Urls.url("https://raw.githubusercontent.com/OpenSceneryX/Library/develop/icon.png");
    }
}
