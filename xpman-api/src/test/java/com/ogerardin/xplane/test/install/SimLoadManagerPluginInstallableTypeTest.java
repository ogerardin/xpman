package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.ArchiveInstallSource;
import com.ogerardin.xplane.plugins.custom.SimLoadManagerPlugin.SimLoadManagerPluginInstallableType;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.util.progress.ProgressListener;
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
 * Tests recognition and installation of SimLoad Manager v5+ plugin archives.
 */
class SimLoadManagerPluginInstallableTypeTest {

    private static final ProgressListener NO_PROGRESS = (ratio, message) -> {};

    @TempDir
    Path tempFolder;

    @Test
    void recognizesArchiveWithSimLoadManagerStructure() throws IOException {
        Path zip = createSimLoadManagerV5Archive();

        assertThat(new SimLoadManagerPluginInstallableType().recognizes(new ZipArchive(zip)), is(true));
    }

    @Test
    void doesNotRecognizeArchiveWithoutSimLoadManagerFolder() throws IOException {
        Path zip = createGenericPluginArchive();

        assertThat(new SimLoadManagerPluginInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void doesNotRecognizeArchiveWithoutLuaFolder() throws IOException {
        Path zip = createSimLoadManagerWithoutLuaFolder();

        assertThat(new SimLoadManagerPluginInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void doesNotRecognizeArchiveWithoutXplFiles() throws IOException {
        Path zip = createSimLoadManagerWithoutXpl();

        assertThat(new SimLoadManagerPluginInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void isDiscoveredAsAnInstallableType() throws IOException {
        Path zip = createSimLoadManagerV5Archive();

        ArchiveInstallSource source = ArchiveInstallSource.ofZip(zip);

        assertThat(source.getInstallableType().isPresent(), is(true));
        assertThat(source.getInstallableType().get(), instanceOf(SimLoadManagerPluginInstallableType.class));
    }

    @Test
    void installsSimLoadManagerPlugin() throws Exception {
        Path zip = createSimLoadManagerV5Archive();
        XPlane xPlane = new XPlane(tempFolder);

        new SimLoadManagerPluginInstallableType().install(xPlane, new ZipArchive(zip), NO_PROGRESS);

        Path installed = xPlane.getPaths().plugins().resolve("SimLoadManager");
        assertThat(Files.exists(installed), is(true));
        assertThat(Files.exists(installed.resolve("mac_x64").resolve("SimLoadManager.xpl")), is(true));
        assertThat(Files.exists(installed.resolve("lua").resolve("SimLoadManager.lua")), is(true));
    }

    @Test
    void warnsAboutLibcurlOnLinux() throws Exception {
        // This test will only produce a warning on Linux systems
        Path zip = createSimLoadManagerV5Archive();
        XPlane xPlane = new XPlane(tempFolder);

        InspectionResult result = new SimLoadManagerPluginInstallableType()
                .preconditions(xPlane, new ZipArchive(zip));

        if (Platforms.getCurrent() == Platforms.LINUX) {
            assertThat(result.getMessages(), hasItem(hasProperty("severity", is(Severity.WARN))));
            assertThat(result.getMessages(), hasItem(hasProperty("object", containsString("libcurl"))));
        }
    }

    @Test
    void preconditionsDoNotRequireFlyWithLua() throws Exception {
        Path zip = createSimLoadManagerV5Archive();
        XPlane xPlane = new XPlane(tempFolder);

        InspectionResult result = new SimLoadManagerPluginInstallableType()
                .preconditions(xPlane, new ZipArchive(zip));

        // No ERROR severity messages should require FlyWithLua
        assertThat(result.getMessages().stream()
                .filter(msg -> msg.getSeverity() == Severity.ERROR)
                .anyMatch(msg -> msg.getMessage().contains("FlyWithLua")), is(false));
    }

    @Test
    void preconditionsDoNotRequireSges() throws Exception {
        Path zip = createSimLoadManagerV5Archive();
        XPlane xPlane = new XPlane(tempFolder);

        InspectionResult result = new SimLoadManagerPluginInstallableType()
                .preconditions(xPlane, new ZipArchive(zip));

        // No ERROR severity messages should require SGES
        assertThat(result.getMessages().stream()
                .filter(msg -> msg.getSeverity() == Severity.ERROR)
                .anyMatch(msg -> msg.getMessage().contains("SGES")), is(false));
    }

    // Helper methods to create test archives

    private Path createSimLoadManagerV5Archive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "simload-v5", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            // Add SimLoadManager directory entry
            out.putNextEntry(new ZipEntry("SimLoadManager/"));
            out.closeEntry();

            // Add lua directory
            out.putNextEntry(new ZipEntry("SimLoadManager/lua/"));
            out.closeEntry();

            // Add lua/SimLoadManager.lua
            out.putNextEntry(new ZipEntry("SimLoadManager/lua/SimLoadManager.lua"));
            out.write("-- SimLoad Manager v5.0.6\n".getBytes());
            out.closeEntry();

            // Add mac_x64 directory
            out.putNextEntry(new ZipEntry("SimLoadManager/mac_x64/"));
            out.closeEntry();

            // Add mac_x64/SimLoadManager.xpl
            out.putNextEntry(new ZipEntry("SimLoadManager/mac_x64/SimLoadManager.xpl"));
            out.write("dummy xpl content".getBytes());
            out.closeEntry();

            // Add skunkcrafts_updater.cfg
            out.putNextEntry(new ZipEntry("SimLoadManager/skunkcrafts_updater.cfg"));
            out.write("version|5.0.6\nmodule|https://example.com\n".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    private Path createGenericPluginArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "generic-plugin", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("SomePlugin/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SomePlugin/mac_x64/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SomePlugin/mac_x64/plugin.xpl"));
            out.write("dummy xpl content".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    private Path createSimLoadManagerWithoutLuaFolder() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "simload-no-lua", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("SimLoadManager/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SimLoadManager/mac_x64/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SimLoadManager/mac_x64/SimLoadManager.xpl"));
            out.write("dummy xpl content".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    private Path createSimLoadManagerWithoutXpl() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "simload-no-xpl", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("SimLoadManager/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SimLoadManager/lua/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SimLoadManager/lua/SimLoadManager.lua"));
            out.write("-- SimLoad Manager\n".getBytes());
            out.closeEntry();
        }
        return zip;
    }
}
