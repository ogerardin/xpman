package com.ogerardin.xplane.aircrafts;

import com.ogerardin.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.util.TimingExtension;
import com.ogerardin.xplane.InvalidConfig;
import com.ogerardin.xplane.XPlaneInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@Slf4j
@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
public class AircraftManagerTest {

    @Test
    public void testLoadAircrafts() throws InvalidConfig {

        XPlaneInstance xplane = new XPlaneInstance(XPlaneInstance.getDefaultXPRootFolder());

        List<Aircraft> aircrafts = xplane.getAircraftManager().loadAircrafts();
        log.info("Found {} aircrafts", aircrafts.size());
        aircrafts.forEach(aircraft -> log.info("*** {} ({})", aircraft.getName(), aircraft.getClass().getName()));

    }

}
