package com.ogerardin.xplane.aircrafts.custom;

import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import lombok.SneakyThrows;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ColimataConcorde extends Aircraft {

    //TODO manage customization (see CUSTOMIZE/description.txt)

    public ColimataConcorde(AcfFile acfFile) throws InstantiationException {
        super(acfFile, "Colimata Concorde FXP");
        IntrospectionHelper.require(getAcfName().equals("CONCORDE FXP"));
    }

    @Override
    public String getVersion() {
        return getNotes();
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

    @Override
    @SneakyThrows
    public Map<String, Path> getManuals() {
        final HashMap<String, Path> manualsMap = new HashMap<>();
        Files.list(getAcfFile().getFile().getParent().resolve("MANUALS"))
                .filter(path -> path.getFileName().toString().endsWith(".pdf"))
                .forEach(path -> manualsMap.put("Manual: " + path.getFileName().toString(), path));
        return manualsMap;
    }

}
