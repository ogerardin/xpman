package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.XPlaneInstance;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NavDataManager extends Manager<NavDataSet> {

    @Getter(lazy = true)
    private final List<NavDataSet> navDataSets = loadNavDataSets();

    public NavDataManager(XPlaneInstance xPlaneInstance) {
        super(xPlaneInstance);
    }

    public List<NavDataSet> loadNavDataSets() {
        return Stream.of(
                simWideOverride(),
                baseNavData(),
                updatedBaseNavData(),
                faaUpdatedApproaches(),
                handPlacedLocalizers(),
                userData()
        ).collect(Collectors.toList());
    }

    private NavDataSet simWideOverride() {
        return new Arinc424DataSet("Sim-wide ARINC424 override", xPlaneInstance.getPaths().customData(), "earth_424.dat");
    }

    private NavDataSet baseNavData() {
        return new XPNavDataSet("Base (shipped with X-Plane)", xPlaneInstance.getPaths().defaultData());
    }

    private NavDataSet updatedBaseNavData() {
        return new XPNavDataSet("Updated base (supplied by third-parties)", xPlaneInstance.getPaths().customData());
    }

    private NavDataSet faaUpdatedApproaches() {
        return new Arinc424DataSet("FAA updated approaches", xPlaneInstance.getPaths().customData(),"FAACIFP18");
    }

    private NavDataSet handPlacedLocalizers() {
        return new XPNavDataSet("Hand-placed localizers",
                xPlaneInstance.getPaths().customScenery().resolve("Global Airports/Earth nav data"),
                new String[] {"earth_nav.dat"}
        );
    }

    private NavDataSet userData() {
        return new XPNavDataSet("User data", xPlaneInstance.getPaths().customData(),
                new String[] {"user_nav.dat", "user_fix.dat"});
    }

}
