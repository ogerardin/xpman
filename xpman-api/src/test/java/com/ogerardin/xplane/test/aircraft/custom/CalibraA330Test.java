package com.ogerardin.xplane.test.aircraft.custom;

import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.aircraft.custom.CalibraA330;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.IntrospectionHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Recognition, version extraction and name-editability of the Calibra Simulations A330-200.
 */
class CalibraA330Test {

    @TempDir
    Path tempDir;

    /** The published ACF ships {@code acf/_studio = "Laminar Research"}; recognition must not use it. */
    private static final String ACF_TEMPLATE = """
            I
            1200 version
            ACF
            PROPERTIES_BEGIN
            P acf/_name Airbus A330-200
            P acf/_studio %s
            P acf/_author %s
            P acf/_version 1.0.0
            PROPERTIES_END
            """;

    @Test
    void recognizedByNameAndAuthor_evenWhenStudioIsLaminarResearch() throws Exception {
        Path acfPath = writeAcf("Laminar Research");
        Aircraft aircraft = IntrospectionHelper.getBestSubclassInstance(Aircraft.class, null, new AcfFile(acfPath));

        assertThat(aircraft, instanceOf(CalibraA330.class));
    }

    @Test
    void notRecognizedWhenAuthorMissingCalibraMarker() throws Exception {
        Path acfPath = writeAcfWith("Laminar Research", "Some Other Author");
        Aircraft aircraft = IntrospectionHelper.getBestSubclassInstance(Aircraft.class, null, new AcfFile(acfPath));

        assertThat(aircraft instanceof CalibraA330, is(false));
    }

    @Test
    void notRecognizedWhenAcfNameDiffers() throws Exception {
        Path acfPath = writeAcfWithCustomName("Airbus A330-300", "Alex Unruh; A330-300: Calibra Simulations");
        Aircraft aircraft = IntrospectionHelper.getBestSubclassInstance(Aircraft.class, null, new AcfFile(acfPath));

        assertThat(aircraft instanceof CalibraA330, is(false));
    }

    @Test
    void displayNameIsFixedAndNotEditable() throws Exception {
        Aircraft aircraft = new CalibraA330(null, new AcfFile(writeAcf("Laminar Research")));

        assertThat(aircraft.getName(), is("A330-200 by Calibra Simulations"));
        assertThat(aircraft.isAcfNameEditable(), is(false));
    }

    @Test
    void versionComesFromVersionTxt() throws Exception {
        Path acfPath = writeAcf("Laminar Research");
        Files.writeString(acfPath.resolveSibling("version.txt"), """
                A330-200 by Calibra Simulations
                version 1.0.0-beta.3
                build 2026-11-16
                """);

        Aircraft aircraft = new CalibraA330(null, new AcfFile(acfPath));

        assertThat(aircraft.getVersion(), is("1.0.0-beta.3"));
    }

    @Test
    void versionFallsBackToAcfWhenVersionTxtIsMissing() throws Exception {
        Aircraft aircraft = new CalibraA330(null, new AcfFile(writeAcf("Laminar Research")));

        assertThat(aircraft.getVersion(), is("1.0.0"));
    }

    private Path writeAcf(String studio) throws Exception {
        return writeAcfWith(studio, "Alex Unruh, Rodrigo Fernandez; A330-200: Calibra Simulations");
    }

    private Path writeAcfWith(String studio, String author) throws Exception {
        Path acfPath = tempDir.resolve("A330-200.acf");
        Files.writeString(acfPath, ACF_TEMPLATE.formatted(studio, author));
        return acfPath;
    }

    private Path writeAcfWithCustomName(String name, String author) throws Exception {
        Path acfPath = tempDir.resolve(name + ".acf");
        Files.writeString(acfPath, """
                I
                1200 version
                ACF
                PROPERTIES_BEGIN
                P acf/_name %s
                P acf/_studio Laminar Research
                P acf/_author %s
                P acf/_version 1.0.0
                PROPERTIES_END
                """.formatted(name, author));
        return acfPath;
    }
}
