package com.ogerardin.xplane.test.aircrafts;

import com.ogerardin.test.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.InvalidConfig;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class AircraftTest {

    @Test
    void testCanInstantiateAcfFile() throws InvalidConfig {
        Path defaultXPRootFolder = XPlane.getDefaultXPRootFolder();
        Path acfPath = defaultXPRootFolder.resolve("Aircraft/Laminar Research/Boeing 737-800/b738.acf");
        AcfFile acfFile = new AcfFile(acfPath);

        XPlane xPlane = new XPlane(defaultXPRootFolder);
        Aircraft aircraft = new Aircraft(xPlane, acfFile);
        
        assertThat(aircraft.getStudio(), is("Laminar Research"));
        assertThat(aircraft.getName(), is("Boeing 737-800"));
        assertThat(aircraft.getDescription(), is("Boeing 737-800"));
        assertThat(aircraft.getCallsign(), is("American"));
        assertThat(aircraft.getTailNumber(), is("N816NN"));
        assertThat(aircraft.getIcaoCode(), is("B738"));
        assertThat(aircraft.getAuthor(), is("Alex Unruh"));
        assertThat(aircraft.getManufacturer(), is("Boeing"));
        assertThat(aircraft.getCategory(), Matchers.is(Aircraft.Category.AIRLINER));
    }

}
