package com.ogerardin.xplane.file;

import com.ogerardin.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.util.TimingExtension;
import com.ogerardin.xplane.XPManTestBase;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class AcfFileTest extends XPManTestBase {

    @Test
    public void testCanInstantiateAcfFile() {
        Path acfPath = getXPRootFolder().resolve("Aircraft/Laminar Research/Boeing B737-800/b738.acf");

        AcfFile acfFile = new AcfFile(acfPath);
        MatcherAssert.assertThat(acfFile.getFileSpecVersion(), is("1100"));
        MatcherAssert.assertThat(acfFile.getStudio(), is("Laminar Research"));
        MatcherAssert.assertThat(acfFile.getName(), is("Boeing 737-800"));
        MatcherAssert.assertThat(acfFile.getDescription(), is("Boeing 737-800"));
        MatcherAssert.assertThat(acfFile.getCallsign(), is("American"));
        MatcherAssert.assertThat(acfFile.getTailNumber(), is("N816NN"));
        MatcherAssert.assertThat(acfFile.getIcaoCode(), is("B738"));
        MatcherAssert.assertThat(acfFile.getAuthor(), is("Alex Unruh"));
        MatcherAssert.assertThat(acfFile.getManufacturer(), is("Boeing"));
        MatcherAssert.assertThat(acfFile.getCategory(), is(AcfFile.Category.AIRLINER));
    }

}