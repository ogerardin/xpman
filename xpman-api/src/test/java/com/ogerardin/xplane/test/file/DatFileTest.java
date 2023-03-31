package com.ogerardin.xplane.test.file;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.DatFile;
import com.ogerardin.xplane.file.data.dat.DatHeader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

//@ExtendWith(TimingExtension.class)
//@DisabledIfNoXPlaneRootFolder
class DatFileTest {

    @Test
    void testCanInstantiateDatFile() {
        Path objPath = XPlane.getDefaultXPRootFolder().resolve("Resources/default data/earth_fix.dat");

        DatFile objFile = new DatFile(objPath);
        DatHeader header = objFile.getData().getHeader();
        assertThat(objFile.getFileSpecVersion(), is("1200"));
    }

}