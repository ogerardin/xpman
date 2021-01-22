package com.ogerardin.xplane.file;

import com.ogerardin.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.util.TimingExtension;
import com.ogerardin.xplane.XPlane;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class ObjFileTest {

    @Test
    public void testCanInstantiateObjFile() {
        Path objPath = XPlane.getDefaultXPRootFolder().resolve("Custom Scenery/Aerosoft - EDDF Frankfurt/Objects/Airport/v01partI12.obj");

        ObjFile objFile = new ObjFile(objPath);
        MatcherAssert.assertThat(objFile.getFileSpecVersion(), is("800"));
    }

}