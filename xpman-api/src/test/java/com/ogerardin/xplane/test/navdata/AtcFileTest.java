package com.ogerardin.xplane.test.navdata;

import com.ogerardin.test.util.EnableOnLocalXPlane12;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.test.util.XPlaneTestUtil;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.exception.InvalidConfig;
import com.ogerardin.xplane.navdata.AtcFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(TimingExtension.class)
@EnableOnLocalXPlane12
class AtcFileTest {

    @Test
    void testAtcFile() throws InvalidConfig {
        XPlane xPlane = new XPlane(XPlaneTestUtil.getDefaultXPRootFolder());

        AtcFile atcFile = xPlane.getNavDataManager().getNavDataSets().stream()
                .flatMap(dataSet -> dataSet.getChildren().stream())
                .filter(AtcFile.class::isInstance)
                .map(AtcFile.class::cast)
                .filter(AtcFile::getExists)
                .findFirst()
                .orElseThrow();

        assertThat(atcFile.getName(), endsWith("atc.dat"));
        assertThat(atcFile.getData().getHeader().getOrigin(), is("A"));
        assertThat(atcFile.getData().getHeader().getSpecVersion(), is("1000"));
        assertThat(atcFile.getData().getHeader().getFileType(), is("ATCFILE"));
        assertThat(atcFile.getDescription(), notNullValue());
    }
}