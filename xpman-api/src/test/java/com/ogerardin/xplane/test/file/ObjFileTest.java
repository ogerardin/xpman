package com.ogerardin.xplane.test.file;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.ObjFile;
import com.ogerardin.xplane.test.DisabledIfNoXPlaneRootFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class ObjFileTest {

    @Test
    void testCanInstantiateObjFile() {
        Path objPath = XPlane.getDefaultXPRootFolder().resolve("Custom Scenery/Aerosoft - EDDF Frankfurt/Objects/Airport/v01partI12.obj");

        ObjFile objFile = new ObjFile(objPath);
        assertThat(objFile.getFileSpecVersion(), is("800"));
    }

}