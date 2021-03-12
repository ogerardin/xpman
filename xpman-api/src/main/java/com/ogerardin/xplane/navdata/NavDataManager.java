package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.XPlane;
import lombok.Getter;
import org.parboiled.common.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NavDataManager extends Manager<NavDataSet> {

    @Getter(lazy = true)
    private final List<NavDataSet> navDataSets = loadNavDataSets();

    public NavDataManager(XPlane xPlane) {
        super(xPlane);
    }

    public List<NavDataSet> loadNavDataSets() {
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
        return new Arinc424DataSet("Sim-wide ARINC424 override", xPlane.getPaths().customData(), "earth_424.dat");
    }

    private NavDataSet baseNavData() {
        return new XPNavDataSet("Base (shipped with X-Plane)", xPlane.getPaths().defaultData());
    }

    private NavDataSet updatedBaseNavData() {
        return new XPNavDataSet("Updated base (supplied by third-parties)", xPlane.getPaths().customData());
    }

    private NavDataSet faaUpdatedApproaches() {
        return new Arinc424DataSet("FAA updated approaches", xPlane.getPaths().customData(),"FAACIFP18");
    }

    private NavDataSet handPlacedLocalizers() {
        return new XPNavDataSet("Hand-placed localizers",
                xPlane.getPaths().customScenery().resolve("Global Airports/Earth nav data"),
                new String[] {"earth_nav.dat"}
        );
    }

    private NavDataSet userData() {
        return new XPNavDataSet("User data", xPlane.getPaths().customData(),
                new String[] {"user_nav.dat", "user_fix.dat"});
    }

}
