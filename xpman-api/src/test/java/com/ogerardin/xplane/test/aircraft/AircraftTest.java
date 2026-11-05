package com.ogerardin.xplane.test.aircraft;

import com.ogerardin.test.util.EnableOnAircraftPresent;
import com.ogerardin.test.util.EnableOnLocalXPlane12;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.test.util.XPlaneTestUtil;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.exception.InvalidConfig;
import com.ogerardin.xplane.file.AcfFile;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
@EnableOnLocalXPlane12
class AircraftTest {

    @Test
    void testCanInstantiateAcfFile() throws InvalidConfig {
        Path defaultXPRootFolder = XPlaneTestUtil.getDefaultXPRootFolder();
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

    @Test
    @EnableOnAircraftPresent("Aircraft/Robin DR401/DR401_CDI155.acf")
    void manualsSubfolderPdfsAreListedAsIndividualEntries() throws InvalidConfig {
        Path root = XPlaneTestUtil.getDefaultXPRootFolder();
        Path acfPath = root.resolve("Aircraft/Robin DR401/DR401_CDI155.acf");
        Aircraft aircraft = new Aircraft(new XPlane(root), new AcfFile(acfPath));

        assertThat(aircraft.getManuals().keySet(), Matchers.containsInAnyOrder(
                "DR401 Checklist Normal operation.pdf",
                "DR401 Flight Manual.pdf",
                "DR401_Checklist_Emergency_ops.pdf"));
    }

}
