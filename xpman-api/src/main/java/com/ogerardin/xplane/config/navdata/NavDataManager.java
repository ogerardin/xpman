package com.ogerardin.xplane.config.navdata;

import com.ogerardin.xplane.config.Manager;
import com.ogerardin.xplane.config.XPlaneInstance;
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
                baseNavDataSet(),
                updatedBaseNavDataSet()
//                updatedApproaches(),
//                handPlacedLocalizers(),
//                userData()
        ).collect(Collectors.toList());
    }

    private NavDataSet updatedBaseNavDataSet() {
        return new BaseNavDataSet(
                getXPlaneInstance().getRootFolder().resolve("Resources").resolve("default data"),
                "Base (shipped with X-Plane)"
        );
    }

    private NavDataSet baseNavDataSet() {
        return new BaseNavDataSet(
                getXPlaneInstance().getRootFolder().resolve("Custom data"),
                "Updated base (supplied by third-parties)"
        );
    }

}
