package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.ArchiveInstallSource;
import com.ogerardin.xplane.install.inspections.ConflictingPluginsInspection;
import com.ogerardin.xplane.install.inspections.WouldOverwriteSubpathInspection;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.plugins.custom.OpenSamPlugin;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    // recognition & universal-installer detection

    @Test
    void recognizesV5Archive() throws IOException {
        assertThat(new OpenSamPlugin.OpenSamPluginInstallableType()
                .recognizes(new ZipArchive(createOpenSamV5Archive())), is(true));
    }

    @Test
    void recognizesPreV5ArchiveAsV4TypeOnly() throws IOException {
        ZipArchive archive = new ZipArchive(createOpenSamArchive(false));
        assertThat(new OpenSamPlugin.OpenSam4PluginInstallableType().recognizes(archive), is(true));
        assertThat(new OpenSamPlugin.OpenSamPluginInstallableType().recognizes(archive), is(false));
    }

    @Test
    void doesNotRecognizeGenericPluginArchive() throws IOException {
        ZipArchive archive = new ZipArchive(createGenericPluginArchive());
        assertThat(new OpenSamPlugin.OpenSamPluginInstallableType().recognizes(archive), is(false));
        assertThat(new OpenSamPlugin.OpenSam4PluginInstallableType().recognizes(archive), is(false));
    }

    @Test
    void universalInstallerDetectsTheV5TypeOverTheGenericPluginType() throws IOException {
        var type = ArchiveInstallSource.ofZip(createOpenSamV5Archive()).getInstallableType();
        assertThat(type.isPresent(), is(true));
        assertThat(type.get(), instanceOf(OpenSamPlugin.OpenSamPluginInstallableType.class));
    }

    @Test
    void universalInstallerDetectsTheV4TypeOverTheGenericPluginType() throws IOException {
        var type = ArchiveInstallSource.ofZip(createOpenSamArchive(false)).getInstallableType();
        assertThat(type.isPresent(), is(true));
        assertThat(type.get(), instanceOf(OpenSamPlugin.OpenSam4PluginInstallableType.class));
    }

    // preconditions (regression: the two-root archive is the original install blocker)

    @Test
    void preconditionsDoNotBlockTheTwoRootArchive() throws Exception {
        Path zip = createOpenSamV5Archive();
        XPlane xPlane = newXPlaneWithSceneryIni();

        var result = new OpenSamPlugin.OpenSamPluginInstallableType()
                .preconditions(xPlane, new ZipArchive(zip));

        assertThat(result.getMessages().stream().noneMatch(InspectionMessage::isError), is(true));
        assertThat(result.isNotAbort(), is(true));
    }

    @Test
    void preconditionsWarnWhenSceneryIniIsAbsent() throws Exception {
        Path zip = createOpenSamV5Archive();
        XPlane xPlane = new XPlane(tempFolder);

        var result = new OpenSamPlugin.OpenSamPluginInstallableType()
                .preconditions(xPlane, new ZipArchive(zip));

        assertThat(result.getMessages(), hasItem(hasProperty("severity", is(Severity.WARN))));
    }

    @Test
    void preconditionsWarnAboutConflictingPlugin() throws Exception {
        Path zip = createOpenSamV5Archive();
        XPlane xPlane = newXPlaneWithSceneryIni();
        createFakePlugin(xPlane, "AutoDGS");

        var result = new OpenSamPlugin.OpenSamPluginInstallableType()
                .preconditions(xPlane, new ZipArchive(zip));

        assertThat(result.getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.WARN)),
                hasProperty("object", is("AutoDGS")))));
    }

    // v4 flavor detection

    @Test
    void flavorMarkerIsReadFromTheArchiveFilename() {
        assertThat(OpenSamPlugin.OpenSam4PluginInstallableType.flavorOf(Path.of("openSAM-XP11-v4.5.5.zip")),
                is(XPlaneMajorVersion.XP11));
        assertThat(OpenSamPlugin.OpenSam4PluginInstallableType.flavorOf(Path.of("openSAM-XP12-v4.5.5.zip")),
                is(XPlaneMajorVersion.XP12));
        assertThat(OpenSamPlugin.OpenSam4PluginInstallableType.flavorOf(Path.of("openSAM-v5.4.1.zip")),
                is(nullValue()));
        assertThat(OpenSamPlugin.OpenSam4PluginInstallableType.flavorOf(Path.of("openSAM.zip")),
                is(nullValue()));
    }

    // install

    @Test
    void installsPluginAndSceneryLibraryAndPositionsTheIniEntry() throws Exception {
        Path zip = createOpenSamV5Archive();
        XPlane xPlane = newXPlaneWithSceneryIni();

        new OpenSamPlugin.OpenSamPluginInstallableType().install(xPlane, new ZipArchive(zip), NO_PROGRESS);

        Path plugin = xPlane.getPaths().plugins().resolve("openSAM");
        assertThat(Files.exists(plugin.resolve("mac_x64").resolve("openSAM.xpl")), is(true));
        assertThat(Files.exists(plugin.resolve("planes.cfg")), is(true));
        Path library = xPlane.getPaths().customScenery().resolve("openSAM_Library");
        assertThat(Files.exists(library.resolve("library.txt")), is(true));
        assertThat(Files.exists(library.resolve("dgs").resolve("Marshaller.obj")), is(true));
        // the archive root's loose distribution docs are not installed anywhere
        assertThat(Files.exists(xPlane.getPaths().customScenery().resolve("README.html")), is(false));

        List<String> lines = iniLines(xPlane);
        assertThat(lines.indexOf("SCENERY_PACK Custom Scenery/openSAM_Library"),
                lessThan(lines.indexOf("SCENERY_PACK *GLOBAL_AIRPORTS*")));
    }

    @Test
    void installsV4PluginAndLibraryEvenWithoutSceneryIni() throws Exception {
        Path zip = createOpenSamArchive(false);
        XPlane xPlane = new XPlane(tempFolder);

        new OpenSamPlugin.OpenSam4PluginInstallableType().install(xPlane, new ZipArchive(zip), NO_PROGRESS);

        assertThat(Files.exists(xPlane.getPaths().plugins().resolve("openSAM").resolve("mac_x64").resolve("openSAM.xpl")), is(true));
        assertThat(Files.exists(xPlane.getPaths().customScenery().resolve("openSAM_Library").resolve("library.txt")), is(true));
        // the Scenery panel's entry list was refreshed even without an ini
        assertThat(xPlane.getSceneryManager().getSceneryEntries().stream()
                .anyMatch(entry -> entry.getSceneryPackage() != null
                        && entry.getSceneryPackage().getFolder().getFileName().toString().equals("openSAM_Library")), is(true));
    }

    // domain class matching

    @Test
    void installedOpenSamPluginIsMatchedToTheSpecializedSubclass() throws Exception {
        Path zip = createOpenSamV5Archive();
        XPlane xPlane = newXPlaneWithSceneryIni();
        new OpenSamPlugin.OpenSamPluginInstallableType().install(xPlane, new ZipArchive(zip), NO_PROGRESS);

        Path xpl = xPlane.getPaths().plugins().resolve("openSAM").resolve("mac_x64").resolve("openSAM.xpl");
        Plugin plugin = IntrospectionHelper.getBestSubclassInstance(Plugin.class, xPlane, xpl);

        assertThat(plugin, instanceOf(OpenSamPlugin.class));
        assertThat(plugin.getName(), is("openSAM"));
    }

    @Test
    void openSamSubclassDoesNotMatchOtherPluginFolders() throws Exception {
        XPlane xPlane = new XPlane(tempFolder);
        createFakePlugin(xPlane, "SomePlugin");
        Path xpl = xPlane.getPaths().plugins().resolve("SomePlugin").resolve("mac_x64").resolve("SomePlugin.xpl");

        Plugin plugin = IntrospectionHelper.getBestSubclassInstance(Plugin.class, xPlane, xpl);

        assertThat(plugin, not(instanceOf(OpenSamPlugin.class)));
    }

    // uninstall

    @Test
    void uninstallRemovesPluginLibraryAndIniEntry() throws Exception {
        Path zip = createOpenSamV5Archive();
        XPlane xPlane = newXPlaneWithSceneryIni();
        new OpenSamPlugin.OpenSamPluginInstallableType().install(xPlane, new ZipArchive(zip), NO_PROGRESS);
        Path xpl = xPlane.getPaths().plugins().resolve("openSAM").resolve("mac_x64").resolve("openSAM.xpl");

        // emulate the trash by deleting for real: the ini entry is only dropped once the
        // library folder is gone from disk
        new OpenSamPlugin(xPlane, xpl) {
            @Override
            protected void moveToTrash(Path path) throws IOException {
                try (var paths = Files.walk(path)) {
                    paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
                }
            }
        }.uninstall();

        assertThat(Files.exists(xPlane.getPaths().plugins().resolve("openSAM")), is(false));
        assertThat(Files.exists(xPlane.getPaths().customScenery().resolve("openSAM_Library")), is(false));
        assertThat(iniLines(xPlane).contains("SCENERY_PACK Custom Scenery/openSAM_Library"), is(false));
    }

    @Test
    void uninstallWarningDetailsMentionTheSceneryLibrary() throws Exception {
        XPlane xPlane = new XPlane(tempFolder);
        createFakePlugin(xPlane, "openSAM");
        Path xpl = xPlane.getPaths().plugins().resolve("openSAM").resolve("mac_x64").resolve("openSAM.xpl");

        Plugin plugin = IntrospectionHelper.getBestSubclassInstance(Plugin.class, xPlane, xpl);

        assertThat(plugin.getUninstallWarningDetails(), containsString("openSAM_Library"));
    }

    // blocking gates: XP12-only v5 and the v4 XP11/XP12 flavor (mocked X-Plane versions)

    @Test
    void blocksV5InstallOnXPlane11() throws Exception {
        var result = new OpenSamPlugin.OpenSamPluginInstallableType()
                .preconditions(versionMock("11.55.1", XPlaneMajorVersion.XP11),
                        new ZipArchive(createOpenSamV5Archive()));

        assertThat(result.isNotAbort(), is(false));
        assertThat(result.getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.ERROR)),
                hasProperty("message", containsString("X-Plane 12")))));
    }

    @Test
    void allowsV5InstallOnXPlane12() throws Exception {
        var result = new OpenSamPlugin.OpenSamPluginInstallableType()
                .preconditions(versionMock("12.0.9", XPlaneMajorVersion.XP12),
                        new ZipArchive(createOpenSamV5Archive()));

        assertThat(result.isNotAbort(), is(true));
        assertThat(result.getMessages().stream().noneMatch(InspectionMessage::isError), is(true));
    }

    @Test
    void blocksWrongFlavorV4Install() throws Exception {
        Path zip = createOpenSamArchive(false, "openSAM-XP11-v4.5.5"); // XP11 flavor...
        var result = new OpenSamPlugin.OpenSam4PluginInstallableType()
                .preconditions(versionMock("12.0.9", XPlaneMajorVersion.XP12), new ZipArchive(zip)); // ...on XP12

        assertThat(result.isNotAbort(), is(false));
        assertThat(result.getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.ERROR)),
                hasProperty("message", containsString("X-Plane 11 flavor")))));
    }

    @Test
    void allowsV4InstallOnXPlane11WithUnflavoredArchive() throws Exception {
        // the "stick to a 4.x release on X-Plane 11" happy path: no flavor token, no XP12 gate
        var result = new OpenSamPlugin.OpenSam4PluginInstallableType()
                .preconditions(versionMock("11.55.1", XPlaneMajorVersion.XP11),
                        new ZipArchive(createOpenSamArchive(false)));

        assertThat(result.isNotAbort(), is(true));
        assertThat(result.getMessages().stream().noneMatch(InspectionMessage::isError), is(true));
    }

    @Test
    void allowsV4InstallWithMatchingFlavor() throws Exception {
        Path zip = createOpenSamArchive(false, "openSAM-XP12-v4.5.5");
        var result = new OpenSamPlugin.OpenSam4PluginInstallableType()
                .preconditions(versionMock("12.0.9", XPlaneMajorVersion.XP12), new ZipArchive(zip));

        assertThat(result.isNotAbort(), is(true));
        assertThat(result.getMessages().stream().noneMatch(InspectionMessage::isError), is(true));
    }

    // reinstall over an existing install

    @Test
    void reinstallingOverAnExistingInstallWarnsAboutBothHalves() throws Exception {
        Path zip = createOpenSamV5Archive();
        XPlane xPlane = newXPlaneWithSceneryIni();
        new OpenSamPlugin.OpenSamPluginInstallableType().install(xPlane, new ZipArchive(zip), NO_PROGRESS);

        var result = new OpenSamPlugin.OpenSamPluginInstallableType()
                .preconditions(xPlane, new ZipArchive(zip));

        List<InspectionMessage> warnings = result.getMessages().stream()
                .filter(message -> message.getSeverity() == Severity.WARN).toList();
        assertThat(warnings.size(), is(2)); // plugin half + library half, and no ERROR
        assertThat(result.getMessages().stream().noneMatch(InspectionMessage::isError), is(true));
    }

    @Test
    void conflictWarningDoesNotTriggerAFullPluginLoad() throws Exception {
        Path zip = createOpenSamV5Archive();
        XPlane xPlane = newXPlaneWithSceneryIni();
        createFakePlugin(xPlane, "SAM");

        new ConflictingPluginsInspection(xPlane, Map.of("SAM", "remove the SAM plugin"))
                .inspect(new ZipArchive(zip));

        // the conflict probe must not build Plugin objects: a full load pre-warms remote
        // Skunkcrafts versions, a stall when triggered from the wizard's FX thread
        assertThat(xPlane.getPluginManager().getItems(), is(nullValue()));
    }

    // helpers

    /** An openSAM v5-style archive: plugin half with the planes.cfg marker, library half, root docs. */
    private Path createOpenSamV5Archive() throws IOException {
        return createOpenSamArchive(true);
    }

    private Path createOpenSamArchive(boolean v5) throws IOException {
        return createOpenSamArchive(v5, "openSAM-v" + (v5 ? "5.4.1" : "4.5.5"));
    }

    private Path createOpenSamArchive(boolean v5, String namePrefix) throws IOException {
        Path zip = Files.createTempFile(tempFolder, namePrefix, ".zip");
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

    /** A generic single-root plugin archive, for the recognition negative cases. */
    private Path createGenericPluginArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "generic-plugin", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            zipEntry(out, "SomePlugin/mac_x64/plugin.xpl", "dummy xpl content");
        }
        return zip;
    }

    /** A fake X-Plane install whose scenery_packs.ini lists an airport and global airports. */
    private XPlane newXPlaneWithSceneryIni() throws Exception {
        Path customScenery = tempFolder.resolve("Custom Scenery");
        Files.createDirectories(customScenery);
        Files.writeString(customScenery.resolve("scenery_packs.ini"),
                "I\n1000 Version\nSCENERY\n\n"
                        + "SCENERY_PACK Custom Scenery/Some Airport/\n"
                        + "SCENERY_PACK *GLOBAL_AIRPORTS*\n");
        return new XPlane(tempFolder);
    }

    private List<String> iniLines(XPlane xPlane) throws IOException {
        return Files.readAllLines(xPlane.getPaths().customScenery().resolve("scenery_packs.ini"))
                .stream().filter(line -> line.startsWith("SCENERY_PACK")).toList();
    }

    /**
     * A mocked X-Plane reporting the given version, delegating paths and managers to a real
     * temp install — the AssertXPlaneVersionAtLeastTest idiom, extended for the
     * plugin-manager-aware preconditions chain.
     */
    private XPlane versionMock(String version, XPlaneMajorVersion major) throws Exception {
        XPlane real = new XPlane(tempFolder);
        XPlane xPlane = mock(XPlane.class);
        when(xPlane.getVersion()).thenReturn(version);
        when(xPlane.getMajorVersion()).thenReturn(major);
        when(xPlane.getPaths()).thenReturn(real.getPaths());
        when(xPlane.getPluginManager()).thenReturn(real.getPluginManager());
        return xPlane;
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
