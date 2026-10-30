package com.ogerardin.xplane.test.aircraft;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.exception.InvalidConfig;
import com.ogerardin.xplane.file.AcfFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/** Tests the selection of files belonging to a single variant, for {@link Aircraft#uninstallVariant()}. */
class AircraftVariantTest {

    @TempDir
    Path tempDir;

    @Test
    void variantFilesAreTheAcfAndItsIconsOnly() throws IOException, InvalidConfig {
        Path acf = Files.writeString(tempDir.resolve("b738.acf"), "acf");
        Files.writeString(tempDir.resolve("b738_icon11.png"), "icon");
        Files.writeString(tempDir.resolve("b738_icon11_thumb.png"), "thumb");
        // folder-level files shared with other variants: must NOT be selected
        Files.createDirectories(tempDir.resolve("liveries/Delta"));
        Files.writeString(tempDir.resolve("b738_700.acf"), "sibling variant");
        Files.writeString(tempDir.resolve("cockpit.obj"), "shared object");

        Aircraft aircraft = new Aircraft(new XPlane(tempDir), new AcfFile(acf));

        assertThat(aircraft.getVariantFiles(), contains(
                acf,
                tempDir.resolve("b738_icon11.png"),
                tempDir.resolve("b738_icon11_thumb.png")));
    }

    @Test
    void missingIconFilesAreSkipped() throws IOException, InvalidConfig {
        Path acf = Files.writeString(tempDir.resolve("b738.acf"), "acf");

        Aircraft aircraft = new Aircraft(new XPlane(tempDir), new AcfFile(acf));

        assertThat(aircraft.getVariantFiles(), contains(acf));
    }
}
