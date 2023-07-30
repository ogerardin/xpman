package com.ogerardin.xpman.test;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.exception.InvalidConfig;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.Maps;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static com.ogerardin.xpman.util.SpelUtil.eval;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Disabled
class SpelTest {

    @Test
    void testSpel() throws InvalidConfig {
        Path defaultXPRootFolder = XPlane.getDefaultXPRootFolder();
        Path acfPath = defaultXPRootFolder.resolve("Aircraft/Laminar Research/Boeing 737-800/b738.acf");
        AcfFile acfFile = new AcfFile(acfPath);

        XPlane xPlane = new XPlane(defaultXPRootFolder);
        Aircraft aircraft = new Aircraft(xPlane, acfFile);
//        aircraft.setEnabled(true);

        assertThat(eval("true", aircraft), is(TRUE));
        assertThat(eval("name", aircraft), is("Boeing 737-800"));
//        assertThat(eval("enabled", aircraft), is(TRUE));
//        assertThat(eval("! enabled", aircraft), is(FALSE));
    }

    @Test
    void testSpelVariables() {
        Map<String, Object> variables = Maps.mapOf("var1", TRUE);

        assertThat(eval("#var1", null, variables), is(TRUE));
    }


}