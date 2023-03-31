package com.ogerardin.xplane.test.file;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.test.DisabledIfNoXPlaneRootFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class AcfFileTest {

    @Test
    void testCanInstantiateAcfFile() {
        Path acfPath = XPlane.getDefaultXPRootFolder().resolve("Aircraft/Laminar Research/Boeing B737-800/b738.acf");

        AcfFile acfFile = new AcfFile(acfPath);
        assertThat(acfFile.getFileSpecVersion(), is("1100"));
    }

}