package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.inspections.ConflictingPluginsInspection;
import com.ogerardin.xplane.install.inspections.WouldOverwriteSubpathInspection;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests recognition, installation and uninstallation of openSAM plugin archives (v5+ and
 * pre-v5), including the openSAM_Library scenery library half and its supporting inspections.
 */
class OpenSamPluginInstallableTypeTest {

    private static final ProgressListener NO_PROGRESS = (ratio, message) -> {};

    @TempDir
    Path tempFolder;

    // WouldOverwriteSubpathInspection

    @Test
    void warnsWhenInstallingTheLibrarySubpathWouldOverwrite() throws Exception {
        Path zip = createOpenSamV5Archive();
        XPlane xPlane = new XPlane(tempFolder);
        Path libraryFolder = xPlane.getPaths().customScenery().resolve("openSAM_Library");
        Files.createDirectories(libraryFolder);
        Files.writeString(libraryFolder.resolve("library.txt"), "existing");

        var result = new WouldOverwriteSubpathInspection(libraryFolder, Path.of("openSAM_Library"))
                .inspect(new ZipArchive(zip));

        assertThat(result.getMessages(), hasItem(hasProperty("severity", is(Severity.WARN))));
    }

    @Test
    void noOverwriteWarningWhenLibraryTargetIsFresh() throws Exception {
        Path zip = createOpenSamV5Archive();
        XPlane xPlane = new XPlane(tempFolder);

        var result = new WouldOverwriteSubpathInspection(
                        xPlane.getPaths().customScenery().resolve("openSAM_Library"), Path.of("openSAM_Library"))
                .inspect(new ZipArchive(zip));

        assertThat(result.getMessages(), empty());
    }

    // ConflictingPluginsInspection

    @Test
    void warnsAboutEachInstalledConflictingPlugin() throws Exception {
        Path zip = createOpenSamV5Archive();
        XPlane xPlane = new XPlane(tempFolder);
        createFakePlugin(xPlane, "SAM");

        var result = new ConflictingPluginsInspection(xPlane, Map.of("SAM", "remove the SAM plugin"))
                .inspect(new ZipArchive(zip));

        assertThat(result.getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.WARN)),
                hasProperty("object", is("SAM")))));
    }

    @Test
    void noConflictWarningWhenConflictingPluginsAreAbsent() throws Exception {
        Path zip = createOpenSamV5Archive();
        XPlane xPlane = new XPlane(tempFolder);

        var result = new ConflictingPluginsInspection(xPlane, Map.of("SAM", "remove the SAM plugin"))
                .inspect(new ZipArchive(zip));

        assertThat(result.getMessages(), empty());
    }

    // helpers

    /** An openSAM v5-style archive: plugin half with the planes.cfg marker, library half, root docs. */
    private Path createOpenSamV5Archive() throws IOException {
        return createOpenSamArchive(true);
    }

    private Path createOpenSamArchive(boolean v5) throws IOException {
        Path zip = Files.createTempFile(tempFolder, "openSAM-v" + (v5 ? "5.4.1" : "4.5.5"), ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            zipEntry(out, "openSAM/mac_x64/openSAM.xpl", "dummy xpl content");
            zipEntry(out, "openSAM/win_x64/openSAM.xpl", "dummy xpl content");
            zipEntry(out, "openSAM/lin_x64/openSAM.xpl", "dummy xpl content");
            zipEntry(out, "openSAM/sound/beep.wav", "dummy sound");
            if (v5) {
                zipEntry(out, "openSAM/planes.cfg", "icao\tA320");
                zipEntry(out, "openSAM/lua/README.txt", "dummy");
            } else {
                zipEntry(out, "openSAM/acf_door_position.txt", "dummy");
                zipEntry(out, "openSAM/objects/Marshaller.obj", "dummy obj");
            }
            zipEntry(out, "openSAM/skunkcrafts_updater.cfg", "version|5.4.1\nmodule|https://example.com\n");
            zipEntry(out, "openSAM_Library/library.txt", "EXPORT dummy library");
            zipEntry(out, "openSAM_Library/dgs/Marshaller.obj", "dummy obj");
            zipEntry(out, "openSAM_Library/skunkcrafts_updater.cfg", "version|5.4.1\nmodule|https://example.com\n");
            // the real zips carry distribution docs at the archive root
            zipEntry(out, "README.html", "dummy");
            zipEntry(out, "LICENSE", "dummy");
        }
        return zip;
    }

    /** Creates Resources/plugins/&lt;name&gt;/ with .xpl binaries for every platform. */
    private void createFakePlugin(XPlane xPlane, String name) throws IOException {
        for (String platformDir : new String[]{"mac_x64", "win_x64", "lin_x64"}) {
            Path xpl = xPlane.getPaths().plugins().resolve(name).resolve(platformDir).resolve(name + ".xpl");
            Files.createDirectories(xpl.getParent());
            Files.writeString(xpl, "dummy xpl content");
        }
    }

    private void zipEntry(ZipOutputStream out, String name, String content) throws IOException {
        out.putNextEntry(new ZipEntry(name));
        out.write(content.getBytes());
        out.closeEntry();
    }
}
