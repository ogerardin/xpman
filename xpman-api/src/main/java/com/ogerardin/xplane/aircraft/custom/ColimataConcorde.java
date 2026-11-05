package com.ogerardin.xplane.aircraft.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.Map;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * Specialization of {@link Aircraft} for the Colimata Concorde FXP, recognized by ACF name.
 */
@SuppressWarnings("unused")
public class ColimataConcorde extends Aircraft {

    //TODO manage customization (see CUSTOMIZE/description.txt)

    public ColimataConcorde(XPlane xPlane, AcfFile acfFile) throws InstantiationException {
        super(xPlane, acfFile, "Colimata Concorde FXP");
        require(getAcfName().equals("CONCORDE FXP"));
    }

    @Override
    public boolean isAcfNameEditable() {
        return false;
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
                        "X-Plane Forum Support Thread", Urls.url("https://forums.x-plane.org/index.php?/forums/forum/477-concorde-fxp/"),
                        "Colimata Homepage", Urls.url("http://www.colimata.com")
                )
        );
    }

}
