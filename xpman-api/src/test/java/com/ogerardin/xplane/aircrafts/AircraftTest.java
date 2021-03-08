package com.ogerardin.xplane.aircrafts;

import com.ogerardin.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.util.TimingExtension;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.AcfFile;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
public class AircraftTest {

    @Test
    public void testCanInstantiateAcfFile() {
        Path acfPath = XPlane.getDefaultXPRootFolder().resolve("Aircraft/Laminar Research/Boeing B737-800/b738.acf");

        AcfFile acfFile = new AcfFile(acfPath);
        Aircraft aircraft = new Aircraft(acfFile);
        
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
