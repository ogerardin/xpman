package com.ogerardin.xplane.config.aircrafts;

import com.ogerardin.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.util.TimingExtension;
import com.ogerardin.xplane.XPManTestBase;
import com.ogerardin.xplane.config.InvalidConfig;
import com.ogerardin.xplane.config.XPlaneInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@Slf4j
@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
public class AircraftManagerTest extends XPManTestBase {

    @Test
    public void testLoadAircrafts() throws InvalidConfig {

        XPlaneInstance xplane = new XPlaneInstance(getXPRootFolder());

        List<Aircraft> aircrafts = xplane.getAircraftManager().getAircrafts();
        log.info("Found {} aircrafts", aircrafts.size());
        aircrafts.forEach(aircraft -> log.info("*** {} ({})", aircraft.getName(), aircraft.getClass().getName()));

    }

}
