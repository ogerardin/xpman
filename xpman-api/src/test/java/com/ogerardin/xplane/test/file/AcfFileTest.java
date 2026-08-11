package com.ogerardin.xplane.test.file;

import com.ogerardin.test.util.EnableOnLocalXPlane11;
import com.ogerardin.test.util.EnableOnLocalXPlane12;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.test.util.XPlaneTestUtil;
import com.ogerardin.xplane.file.AcfFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
class AcfFileTest {

    @Test
    @EnableOnLocalXPlane11
    void testCanInstantiateXp11AcfFile() {
        Path acfPath = XPlaneTestUtil.getDefaultXPRootFolder().resolve("Aircraft/Laminar Research/Boeing B737-800/b738.acf");

        AcfFile acfFile = new AcfFile(acfPath);
        assertThat(acfFile.getFileSpecVersion(), is("1100"));
    }

    @Test
    @EnableOnLocalXPlane12
    void testCanInstantiateXp12AcfFile() {
        Path acfPath = XPlaneTestUtil.getDefaultXPRootFolder().resolve("Aircraft/Laminar Research/Boeing 737-800/b738.acf");

        AcfFile acfFile = new AcfFile(acfPath);
        assertThat(acfFile.getFileSpecVersion(), is("1200"));
    }

}