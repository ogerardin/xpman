package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.install.InstallableArchive;
import com.ogerardin.xplane.install.ProgressListener;
import com.ogerardin.xplane.util.AsyncHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class NavDataManager extends Manager<NavDataSet> implements InstallTarget {

    private List<NavDataSet> navDataSets = null;

    public NavDataManager(XPlane xPlane) {
        super(xPlane);
    }

    public List<NavDataSet> getNavDataSets() {
        if (navDataSets == null) {
            loadNavDataSets();
        }
        return Collections.unmodifiableList(navDataSets);
    }

    public void reload() {
        AsyncHelper.runAsync(this::loadNavDataSets);
    }


    public List<NavDataSet> loadNavDataSets() {

        log.info("Loading nav data sets...");
        fireEvent(new ManagerEvent.Loading<>());

        // See https://developer.x-plane.com/article/navdata-in-x-plane-11
        navDataSets = Stream.of(
                simWideOverride(),
                baseNavData(),
                updatedBaseNavData(),
                faaUpdatedApproaches(),
                handPlacedLocalizers(),
                userData()
        ).collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));

        log.info("Loaded {} nav data sets", navDataSets.size());
        fireEvent(new ManagerEvent.Loaded<>(navDataSets));

        return navDataSets;
    }

    private NavDataSet simWideOverride() {
        return new Arinc424DataSet("Sim-wide ARINC424 override",
                xPlane, xPlane.getPaths().customData(), "earth_424.dat");
    }

    private NavDataSet baseNavData() {
        return new XPNavDataSet("Base (shipped with X-Plane)",
                xPlane, xPlane.getPaths().defaultData());
    }

    private NavDataSet updatedBaseNavData() {
        return new XPNavDataSet("Updated base (supplied by third-parties)",
                xPlane, xPlane.getPaths().customData());
    }

    private NavDataSet faaUpdatedApproaches() {
        return new Arinc424DataSet("FAA updated approaches",
                xPlane, xPlane.getPaths().customData(),"FAACIFP18");
    }

    private NavDataSet handPlacedLocalizers() {
        return new XPNavDataSet("Hand-placed localizers",
                xPlane, xPlane.getPaths().customScenery().resolve("Global Airports/Earth nav data"),
                "earth_nav.dat");
    }

    private NavDataSet userData() {
        return new XPNavDataSet("User data",
                xPlane, xPlane.getPaths().customData(), "user_nav.dat", "user_fix.dat");
    }

    @Override
    public void install(InstallableArchive archive, ProgressListener progressListener) throws IOException {
        archive.installTo(xPlane.getPaths().customData(), progressListener);
        reload();
    }
}
