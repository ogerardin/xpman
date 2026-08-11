package com.ogerardin.xplane.test.file;

import com.ogerardin.test.util.EnableOnSceneryPresent;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.test.util.XPlaneTestUtil;
import com.ogerardin.xplane.file.ObjFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
class ObjFileTest {

    @Test
    @EnableOnSceneryPresent("Custom Scenery/Aerosoft - EDDF Frankfurt/Objects/Airport/v01partI12.obj")
    void testCanInstantiateObjFile() {
        Path objPath = XPlaneTestUtil.getDefaultXPRootFolder().resolve("Custom Scenery/Aerosoft - EDDF Frankfurt/Objects/Airport/v01partI12.obj");

        ObjFile objFile = new ObjFile(objPath);
        assertThat(objFile.getFileSpecVersion(), is("800"));
    }

}