package com.ogerardin.xpman.test;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.Maps;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static com.ogerardin.xpman.util.SpelUtil.eval;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Disabled
class SpelTest {

    @Test
    public void testSpel() {
        Path acfPath = XPlaneInstance.getDefaultXPRootFolder().resolve("Aircraft/Laminar Research/Boeing B737-800/b738.acf");

        AcfFile acfFile = new AcfFile(acfPath);
        Aircraft aircraft = new Aircraft(acfFile);
        aircraft.setEnabled(true);

        assertThat(eval("true", aircraft), is(TRUE));
        assertThat(eval("name", aircraft), is("Boeing 737-800"));
        assertThat(eval("enabled", aircraft), is(TRUE));
        assertThat(eval("! enabled", aircraft), is(FALSE));
    }

    @Test
    public void testSpelVariables() {
        Map<String, Object> variables = Maps.mapOf("var1", TRUE);

        assertThat(eval("#var1", null, variables), is(TRUE));
    }


}