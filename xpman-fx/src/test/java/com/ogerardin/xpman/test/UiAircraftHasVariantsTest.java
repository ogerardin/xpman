package com.ogerardin.xpman.test;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xpman.panels.aircraft.UiAircraft;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests that {@link UiAircraft#isMultiVariant()} reflects the number of variants visible in the
 * aircraft panel for the same folder: more than one visible variant (AI variants excluded)
 * makes the "Uninstall this variant" option available.
 */
class UiAircraftHasVariantsTest {

    @TempDir
    Path tempDir;

    @Test
    void hasVariantsTrueWhenMultipleVisibleVariantsShareTheFolder() throws Exception {
        Path plane = Files.createDirectories(tempDir.resolve("Aircraft/Boeing 737-800"));
        Files.writeString(plane.resolve("b738.acf"), "acf");
        Files.writeString(plane.resolve("b738_700.acf"), "acf");

        assertThat(uiAircraft(plane, "b738.acf").isMultiVariant(), is(true));
    }

    @Test
    void hasVariantsFalseWhenOnlySiblingIsAiVariant() throws Exception {
        Path plane = Files.createDirectories(tempDir.resolve("Aircraft/Boeing 737-800"));
        Files.writeString(plane.resolve("b738.acf"), "acf");
        Files.writeString(plane.resolve("b738_AI.acf"), "acf"); // not visible in the panel

        assertThat(uiAircraft(plane, "b738.acf").isMultiVariant(), is(false));
    }

    @Test
    void hasVariantsFalseForSingleVariant() throws Exception {
        Path plane = Files.createDirectories(tempDir.resolve("Aircraft/Cessna 172"));
        Files.writeString(plane.resolve("c172.acf"), "acf");

        assertThat(uiAircraft(plane, "c172.acf").isMultiVariant(), is(false));
    }

    private UiAircraft uiAircraft(Path planeFolder, String acfName) throws Exception {
        Aircraft aircraft = new Aircraft(new XPlane(tempDir), new AcfFile(planeFolder.resolve(acfName)));
        return new UiAircraft(aircraft);
    }
}
