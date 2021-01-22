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

import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
public class AircraftTest {

    @Test
    public void testCanInstantiateAcfFile() {
        Path acfPath = XPlane.getDefaultXPRootFolder().resolve("Aircraft/Laminar Research/Boeing B737-800/b738.acf");

        AcfFile acfFile = new AcfFile(acfPath);
        Aircraft aircraft = new Aircraft(acfFile);
        
        MatcherAssert.assertThat(aircraft.getStudio(), is("Laminar Research"));
        MatcherAssert.assertThat(aircraft.getName(), is("Boeing 737-800"));
        MatcherAssert.assertThat(aircraft.getDescription(), is("Boeing 737-800"));
        MatcherAssert.assertThat(aircraft.getCallsign(), is("American"));
        MatcherAssert.assertThat(aircraft.getTailNumber(), is("N816NN"));
        MatcherAssert.assertThat(aircraft.getIcaoCode(), is("B738"));
        MatcherAssert.assertThat(aircraft.getAuthor(), is("Alex Unruh"));
        MatcherAssert.assertThat(aircraft.getManufacturer(), is("Boeing"));
        MatcherAssert.assertThat(aircraft.getCategory(), Matchers.is(Aircraft.Category.AIRLINER));
    }

}
