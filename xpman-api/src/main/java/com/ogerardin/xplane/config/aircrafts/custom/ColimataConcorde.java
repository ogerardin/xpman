package com.ogerardin.xplane.config.aircrafts.custom;

import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import lombok.SneakyThrows;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ColimataConcorde extends Aircraft {

    //TODO manage customization (see CUSTOMIZE/description.txt)

    public ColimataConcorde(AcfFile acfFile) throws InstantiationException {
        super(acfFile, "Colimata Concorde FXP");
        IntrospectionHelper.require(getAcfName().equals("CONCORDE FXP"));
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.merge(
                super.getLinks(),
                Maps.mapOf(
                        "X-Plane Forum Support Thread", new URL("https://forums.x-plane.org/index.php?/forums/forum/477-concorde-fxp/"),
                        "Colimata Homepage", new URL("http://www.colimata.com")
                )
        );
    }

    @SneakyThrows
    public Map<String, URL> getManuals() {
        final HashMap<String, URL> manualsMap = new HashMap<>();
        Files.list(getAcfFile().getFile().getParent().resolve("MANUALS"))
                .filter(path -> path.getFileName().toString().endsWith(".pdf"))
                .forEach(path -> {
                    try {
                        manualsMap.put("Manual: " + path.getFileName().toString(), path.toUri().toURL());
                    } catch (MalformedURLException ignored) {
                    }
                });
        return manualsMap;
    }

}
