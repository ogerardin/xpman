package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.ArchiveInstallSource;
import com.ogerardin.xplane.plugins.custom.lua.SimLoadManager.SimLoadManagerInstallableType;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests that the v4 script-type SimLoadManagerInstallableType correctly recognizes
 * old-style script archives and doesn't require SGES.
 */
class SimLoadManagerScriptInstallableTypeTest {

    @TempDir
    Path tempFolder;

    @Test
    void recognizesV4ScriptArchive() throws IOException {
        Path zip = createV4ScriptArchive();

        assertThat(new SimLoadManagerInstallableType().recognizes(new ZipArchive(zip)), is(true));
    }

    @Test
    void doesNotRecognizeV5PluginArchive() throws IOException {
        Path zip = createV5PluginArchive();

        // V5 has .xpl files, so the script type should NOT recognize it
        assertThat(new SimLoadManagerInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void isDiscoveredForV4Archive() throws IOException {
        Path zip = createV4ScriptArchive();

        ArchiveInstallSource source = ArchiveInstallSource.ofZip(zip);

        assertThat(source.getInstallableType().isPresent(), is(true));
        assertThat(source.getInstallableType().get(), instanceOf(SimLoadManagerInstallableType.class));
    }

    @Test
    void isDiscoveredAsPluginTypeForV5Archive() throws IOException {
        Path zip = createV5PluginArchive();

        ArchiveInstallSource source = ArchiveInstallSource.ofZip(zip);

        assertThat(source.getInstallableType().isPresent(), is(true));
        // V5 should be recognized as plugin type, not script type
        assertThat(source.getInstallableType().get(), not(instanceOf(SimLoadManagerInstallableType.class)));
    }

    @Test
    void preconditionsDoNotRequireSges() throws Exception {
        Path zip = createV4ScriptArchive();
        XPlane xPlane = new XPlane(tempFolder);

        InspectionResult result = new SimLoadManagerInstallableType()
                .preconditions(xPlane, new ZipArchive(zip));

        // No ERROR severity messages should require SGES
        assertThat(result.getMessages().stream()
                .filter(msg -> msg.getSeverity() == Severity.ERROR)
                .anyMatch(msg -> msg.getMessage().contains("SGES")), is(false));
    }

    // Helper methods

    private Path createV4ScriptArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "simload-v4", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("SimLoadManager.lua"));
            out.write("-- SimLoad Manager v4.x\n".getBytes());
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SLM-Data/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SLM-Data/settings.txt"));
            out.write("simbrief_id=123\n".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    private Path createV5PluginArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "simload-v5", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("SimLoadManager/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SimLoadManager/lua/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SimLoadManager/lua/SimLoadManager.lua"));
            out.write("-- SimLoad Manager v5.0.6\n".getBytes());
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SimLoadManager/mac_x64/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SimLoadManager/mac_x64/SimLoadManager.xpl"));
            out.write("dummy xpl content".getBytes());
            out.closeEntry();
        }
        return zip;
    }
}
