package com.ogerardin.xplane.test.aircraft;

import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.aircraft.custom.ColimataConcorde;
import com.ogerardin.xplane.aircraft.custom.Dmax3D_EF2000;
import com.ogerardin.xplane.aircraft.custom.FlightFactorA350;
import com.ogerardin.xplane.file.AcfFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AircraftNameEditabilityTest {

    @TempDir
    Path tempDir;

    @Test
    void normalAircraftNameCanBeEdited() {
        assertTrue(new Aircraft(null, null).canEditAcfName());
    }

    @Test
    void customDisplayNameAircraftNamesCannotBeEdited() throws Exception {
        assertFalse(aircraft("Airbus a350 XP11", "FlightFactor", FlightFactorA350.class).canEditAcfName());
        assertFalse(aircraft("Eurofighter Typhoon", "dmax3d.com", Dmax3D_EF2000.class).canEditAcfName());
        assertFalse(aircraft("CONCORDE FXP", "Colimata", ColimataConcorde.class).canEditAcfName());
    }

    private <T extends Aircraft> T aircraft(String name, String studio, Class<T> type) throws Exception {
        Path file = tempDir.resolve(type.getSimpleName() + ".acf");
        Files.writeString(file, """
                I
                1200 version
                ACF
                PROPERTIES_BEGIN
                P acf/_name %s
                P acf/_studio %s
                PROPERTIES_END
                """.formatted(name, studio));
        return type.getConstructor(com.ogerardin.xplane.XPlane.class, AcfFile.class)
                .newInstance(null, new AcfFile(file));
    }
}
