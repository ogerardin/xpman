package com.ogerardin.xplane.config.aircrafts.custom;

import com.ogerardin.xplane.config.LinkType;
import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.Maps;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.Map;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

@SuppressWarnings("unused")
public class ColimataConcorde extends Aircraft {

    public ColimataConcorde(AcfFile acfFile) throws InstantiationException {
        super(acfFile, "Colimata Concorde FXP");
        assertTrue(getName().equals("CONCORDE FXP"));
    }

    @SneakyThrows
    @Override
    public Map<LinkType, URL> getLinks() {
        return Maps.mapOf(
                LinkType.SUPPORT, new URL("https://forums.x-plane.org/index.php?/forums/forum/477-concorde-fxp/"),
                LinkType.HOMEPAGE, new URL("http://www.colimata.com")
        );
    }

}
