package com.ogerardin.xplane.test.aircraft;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.custom.XpfrAircraft;
import com.ogerardin.xplane.file.AcfFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XpfrAircraftTest {

    @Test
    void displaysVersionWithoutIdAndKeepsFullIdForLink(@TempDir Path xplaneRoot) throws Exception {
        Path aircraftFolder = xplaneRoot.resolve("Aircraft/Test");
        Files.createDirectories(aircraftFolder);
        Path acf = aircraftFolder.resolve("test.acf");
        Files.writeString(acf, """
                I
                1200 Version
                ACF
                PROPERTIES_BEGIN
                P acf/_studio Test Studio
                PROPERTIES_END
                """);
        Files.createFile(aircraftFolder.resolve("AlphaJet-AJET.v.17-345_(33)_179.txt"));

        var aircraft = new XpfrAircraft(new XPlane(xplaneRoot), new AcfFile(acf));

        assertEquals("17-345_(33)_179", aircraft.getVersion());
        assertTrue(aircraft.getLinks().get("Aircraft detailed sheet on xpfr.org").toString()
                .contains("seek=AJET.v.17-345_"));
    }

    @Test
    void selectsHighestVersionAmongIdFiles(@TempDir Path xplaneRoot) throws Exception {
        Path aircraftFolder = xplaneRoot.resolve("Aircraft/Test");
        Files.createDirectories(aircraftFolder);
        Path acf = aircraftFolder.resolve("test.acf");
        Files.writeString(acf, """
                I
                1200 Version
                ACF
                PROPERTIES_BEGIN
                P acf/_studio Test Studio
                PROPERTIES_END
                """);
        Files.createFile(aircraftFolder.resolve("DR400-140B-DR40.v.15-153_(33).txt"));
        Files.createFile(aircraftFolder.resolve("DR400-140B-DR40.v.23-319_(33).txt"));

        var aircraft = new XpfrAircraft(new XPlane(xplaneRoot), new AcfFile(acf));

        assertEquals("23-319_(33)", aircraft.getVersion());
    }
}
