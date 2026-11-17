package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.ArchiveInstallSource;
import com.ogerardin.xplane.install.types.SceneryInstallableType;
import com.ogerardin.xplane.install.types.SceneryLibraryInstallableType;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests recognition, detection priority and installation of scenery archives, including the
 * scenery library subtype and the post-install scenery_packs.ini registration.
 */
class SceneryInstallableTypeTest {

    private static final ProgressListener NO_PROGRESS = (ratio, message) -> {};

    @TempDir
    Path tempFolder;

    // recognition

    @Test
    void recognizesEarthNavDataArchive() throws IOException {
        assertThat(new SceneryInstallableType().recognizes(new ZipArchive(createSceneryArchive())), is(true));
    }

    @Test
    void doesNotRecognizePlainLibraryArchive() throws IOException {
        assertThat(new SceneryInstallableType().recognizes(new ZipArchive(createLibraryArchive())), is(false));
    }

    @Test
    void librarySubtypeRecognizesLibraryTxt() throws IOException {
        assertThat(new SceneryLibraryInstallableType().recognizes(new ZipArchive(createLibraryArchive())), is(true));
    }

    @Test
    void librarySubtypeAlsoRecognizesArchiveWithEarthNavData() throws IOException {
        // a mixed archive with both Earth nav data and library.txt is still a valid library
        assertThat(new SceneryLibraryInstallableType().recognizes(new ZipArchive(createMixedSceneryArchive())), is(true));
    }

    // universal-installer detection (deepest-inheritance-wins)

    @Test
    void universalInstallerDetectsSceneryType() throws IOException {
        var type = ArchiveInstallSource.ofZip(createSceneryArchive()).getInstallableType();
        assertThat(type.isPresent(), is(true));
        assertThat(type.get(), instanceOf(SceneryInstallableType.class));
    }

    @Test
    void universalInstallerDetectsLibraryType() throws IOException {
        var type = ArchiveInstallSource.ofZip(createLibraryArchive()).getInstallableType();
        assertThat(type.isPresent(), is(true));
        assertThat(type.get(), instanceOf(SceneryLibraryInstallableType.class));
    }

    @Test
    void universalInstallerPrefersLibrarySubtypeForMixedArchive() throws IOException {
        // Earth nav data + library.txt → SceneryLibraryInstallableType wins by inheritance depth
        var type = ArchiveInstallSource.ofZip(createMixedSceneryArchive()).getInstallableType();
        assertThat(type.isPresent(), is(true));
        assertThat(type.get(), instanceOf(SceneryLibraryInstallableType.class));
    }

    // preconditions

    @Test
    void preconditionsDoNotBlockSingleRootArchive() throws Exception {
        XPlane xPlane = newXPlaneWithSceneryIni();

        var result = new SceneryInstallableType().preconditions(xPlane, new ZipArchive(createSceneryArchive()));

        assertThat(result.isNotAbort(), is(true));
        assertThat(result.getMessages().stream().noneMatch(msg -> msg.getSeverity() == Severity.ERROR), is(true));
    }

    @Test
    void preconditionsWarnWhenSceneryIniIsAbsent() throws Exception {
        XPlane xPlane = new XPlane(tempFolder); // no scenery_packs.ini

        var result = new SceneryInstallableType().preconditions(xPlane, new ZipArchive(createSceneryArchive()));

        assertThat(result.getMessages(), hasItem(hasProperty("severity", is(Severity.WARN))));
        assertThat(result.isNotAbort(), is(true)); // warning, not blocking
    }

    @Test
    void noIniWarningWhenIniExists() throws Exception {
        XPlane xPlane = newXPlaneWithSceneryIni();

        var result = new SceneryInstallableType().preconditions(xPlane, new ZipArchive(createSceneryArchive()));

        assertThat(result.getMessages().stream()
                .noneMatch(msg -> msg.getMessage().contains("scenery_packs.ini")), is(true));
    }

    // install

    @Test
    void installExtractsAndRegistersSceneryAtTheBottom() throws Exception {
        XPlane xPlane = newXPlaneWithSceneryIni("SCENERY_PACK Custom Scenery/Some Airport/");

        new SceneryInstallableType().install(xPlane, new ZipArchive(createSceneryArchive()), NO_PROGRESS);

        // the root folder was extracted
        Path installed = xPlane.getPaths().customScenery().resolve("MisterX_Library");
        assertThat(Files.exists(installed.resolve("library.txt")), is(true));

        // and registered at the bottom of scenery_packs.ini
        List<String> lines = iniLines(xPlane);
        assertThat(lines, hasItem("SCENERY_PACK Custom Scenery/MisterX_Library"));
        int lastIndex = lines.size() - 1;
        assertThat(lines.get(lastIndex), is("SCENERY_PACK Custom Scenery/MisterX_Library"));
    }

    @Test
    void installDoesNotDuplicateWhenAlreadyListed() throws Exception {
        XPlane xPlane = newXPlaneWithSceneryIni(
                "SCENERY_PACK Custom Scenery/Some Airport/",
                "SCENERY_PACK Custom Scenery/MisterX_Library");
        // simulate prior install
        Path installed = xPlane.getPaths().customScenery().resolve("MisterX_Library");
        Files.createDirectories(installed);
        Files.writeString(installed.resolve("library.txt"), "dummy");

        new SceneryInstallableType().install(xPlane, new ZipArchive(createSceneryArchive()), NO_PROGRESS);

        List<String> lines = iniLines(xPlane);
        assertThat(lines.stream().filter(l -> l.contains("MisterX_Library")).count(), is(1L));
    }

    @Test
    void installSucceedsEvenWithoutSceneryIni() throws Exception {
        XPlane xPlane = new XPlane(tempFolder); // no scenery_packs.ini

        new SceneryInstallableType().install(xPlane, new ZipArchive(createSceneryArchive()), NO_PROGRESS);

        // the folder was still extracted
        Path installed = xPlane.getPaths().customScenery().resolve("MisterX_Library");
        assertThat(Files.exists(installed.resolve("library.txt")), is(true));
        // the entry list was refreshed so the new pack shows up in the Scenery panel
        assertThat(xPlane.getSceneryManager().getSceneryEntries().stream()
                .anyMatch(entry -> entry.getSceneryPackage() != null
                        && entry.getSceneryPackage().getFolder().getFileName().toString().equals("MisterX_Library")), is(true));
    }

    // library subtype install uses the same base install

    @Test
    void librarySubtypeInstallsAndRegistersLikeTheBaseType() throws Exception {
        XPlane xPlane = newXPlaneWithSceneryIni();

        new SceneryLibraryInstallableType().install(xPlane, new ZipArchive(createLibraryArchive()), NO_PROGRESS);

        Path installed = xPlane.getPaths().customScenery().resolve("R2_Library");
        assertThat(Files.exists(installed.resolve("library.txt")), is(true));
        assertThat(iniLines(xPlane), hasItem("SCENERY_PACK Custom Scenery/R2_Library"));
    }

    // helpers

    private XPlane newXPlaneWithSceneryIni(String... extraEntries) throws Exception {
        Path customScenery = tempFolder.resolve("Custom Scenery");
        Files.createDirectories(customScenery);
        String ini = "I\n1000 Version\nSCENERY\n\n" + String.join("\n", extraEntries) + "\n";
        Files.writeString(customScenery.resolve("scenery_packs.ini"), ini);
        return new XPlane(tempFolder);
    }

    private List<String> iniLines(XPlane xPlane) throws IOException {
        return Files.readAllLines(xPlane.getPaths().customScenery().resolve("scenery_packs.ini"))
                .stream().filter(line -> line.startsWith("SCENERY_PACK")).toList();
    }

    /** A scenery zip with Earth nav data at root/1 level (e.g. MisterX_Library). */
    private Path createSceneryArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "MisterX_Library_2.0c", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            zipEntry(out, "MisterX_Library/library.txt", "EXPORT dummy library");
            zipEntry(out, "MisterX_Library/Earth nav data/", null);
            zipEntry(out, "MisterX_Library/Earth nav data/+40-070/1234567.dsf", "dummy dsf");
            zipEntry(out, "MisterX_Library/Objects/some.obj", "dummy obj");
            zipEntry(out, "MisterX_Library/version.txt", "2.0c");
        }
        return zip;
    }

    /** A pure library zip — no Earth nav data, only library.txt at root. */
    private Path createLibraryArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "R2_Library", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            zipEntry(out, "R2_Library/library.txt", "EXPORT dummy");
            zipEntry(out, "R2_Library/Objects/some.obj", "dummy obj");
            zipEntry(out, "R2_Library/version.txt", "1.0");
        }
        return zip;
    }

    /** A mixed archive with both Earth nav data and library.txt. */
    private Path createMixedSceneryArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "Mixed_Scenery", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            zipEntry(out, "Mixed_Scenery/library.txt", "EXPORT dummy");
            zipEntry(out, "Mixed_Scenery/Earth nav data/", null);
            zipEntry(out, "Mixed_Scenery/Earth nav data/+40-070/1234567.dsf", "dummy dsf");
            zipEntry(out, "Mixed_Scenery/Objects/some.obj", "dummy obj");
        }
        return zip;
    }

    private void zipEntry(ZipOutputStream out, String name, String content) throws IOException {
        out.putNextEntry(new ZipEntry(name));
        if (content != null) {
            out.write(content.getBytes());
        }
        out.closeEntry();
    }
}
