package com.ogerardin.xplane.test.aircraft;

import com.ogerardin.test.util.EnableOnLocalXPlane;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.test.util.XPlaneTestUtil;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.exception.InvalidConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@Slf4j
@ExtendWith(TimingExtension.class)
@EnableOnLocalXPlane
class AircraftManagerTest {

    @Test
    void testLoadAircrafts() throws InvalidConfig {

        XPlane xplane = new XPlane(XPlaneTestUtil.getDefaultXPRootFolder());

        List<Aircraft> aircrafts = xplane.getAircraftManager().getAircrafts();
        log.info("Found {} aircraft", aircrafts.size());
        aircrafts.forEach(aircraft -> log.info("*** {} ({})", aircraft.getName(), aircraft.getClass().getName()));

    }

}
