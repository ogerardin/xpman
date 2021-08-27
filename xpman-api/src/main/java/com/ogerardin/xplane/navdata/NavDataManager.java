package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.install.InstallableArchive;
import com.ogerardin.xplane.install.ProgressListener;
import lombok.Getter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NavDataManager extends Manager<NavDataSet> implements InstallTarget {

    @Getter(lazy = true)
    private final List<NavDataSet> navDataSets = loadNavDataSets();

    public NavDataManager(XPlane xPlane) {
        super(xPlane);
    }

    public List<NavDataSet> loadNavDataSets() {
        // See https://developer.x-plane.com/article/navdata-in-x-plane-11
        return Stream.of(
                simWideOverride(),
                baseNavData(),
                updatedBaseNavData(),
                faaUpdatedApproaches(),
                handPlacedLocalizers(),
                userData()
        ).collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
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
        //TODO
        //reload();
    }
}
