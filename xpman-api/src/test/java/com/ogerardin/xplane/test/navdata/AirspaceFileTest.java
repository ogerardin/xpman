package com.ogerardin.xplane.test.navdata;

import com.ogerardin.test.util.EnableOnLocalXPlane12;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.test.util.XPlaneTestUtil;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.exception.InvalidConfig;
import com.ogerardin.xplane.navdata.AirspaceFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
@EnableOnLocalXPlane12
class AirspaceFileTest {

    @Test
    void testAirspaceFile() throws InvalidConfig {
        XPlane xPlane = new XPlane(XPlaneTestUtil.getDefaultXPRootFolder());

        AirspaceFile airspaceFile = xPlane.getNavDataManager().getNavDataSets().stream()
                .flatMap(dataSet -> dataSet.getChildren().stream())
                .filter(AirspaceFile.class::isInstance)
                .map(AirspaceFile.class::cast)
                .filter(AirspaceFile::getExists)
                .findFirst()
                .orElseThrow();

        assertThat(airspaceFile.getName(), endsWith("airspace.txt"));
        assertThat(airspaceFile.getExists(), is(true));
        assertThat(Files.exists(airspaceFile.getPath()), is(true));
    }
}