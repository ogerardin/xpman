package com.ogerardin.xplane.test.update;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.plugins.XPlaneOrgPlugin;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.plugins.custom.OpenSamPlugin;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateProvider;
import com.ogerardin.xplane.update.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

/** Selection keeps assigned protocols authoritative without leaking them into item interfaces. */
class UpdateSelectionTest {
    @TempDir Path root;

    @Test
    void ivaoProviderIsCachedAndSkunkcraftsRemainsAuthoritative() throws Exception {
        var binary = root.resolve("Resources/plugins/XIvAp/64/X-IvAp-64.xpl");
        Files.createDirectories(binary.getParent());
        Files.writeString(binary, "");
        var plugin = new com.ogerardin.xplane.plugins.custom.XIvAp(new XPlane(root), binary);
        assertSame(plugin.getUpdateProvider().orElseThrow(), plugin.getUpdateProvider().orElseThrow());
        Files.writeString(binary.getParent().getParent().resolve("skunkcrafts_updater.cfg"),
                "version|1.0\nmodule|https://example.com\nlocked|true\n");
        var reloaded = new com.ogerardin.xplane.plugins.custom.XIvAp(new XPlane(root), binary);
        assertInstanceOf(SkunkcraftsUpdateProvider.class, reloaded.getUpdateProvider().orElseThrow());
        assertEquals(UpdateStatus.State.LOCKED, reloaded.getUpdateStatus().state());
    }

    @Test
    void ziboAlwaysUsesGoogleDriveEvenWhenSkunkcraftsIsConfigured() throws Exception {
        var folder = root.resolve("Aircraft/Zibo");
        Files.createDirectories(folder);
        var acf = folder.resolve("test.acf");
        Files.writeString(acf, """
                I
                1200 version
                ACF
                PROPERTIES_BEGIN
                P acf/_name Test
                P acf/_notes ZIBOmod test
                PROPERTIES_END
                """);
        Files.writeString(folder.resolve("skunkcrafts_updater.cfg"),
                "version|1.0\nmodule|https://example.com\nlocked|true\n");
        var xPlane = new XPlane(root) {
            @Override public com.ogerardin.xplane.XPlaneMajorVersion getMajorVersion() {
                return com.ogerardin.xplane.XPlaneMajorVersion.XP12;
            }
        };
        var aircraft = new com.ogerardin.xplane.aircraft.custom.ZiboMod738(xPlane,
                new com.ogerardin.xplane.file.AcfFile(acf));
        var provider = aircraft.getUpdateProvider().orElseThrow();
        assertInstanceOf(com.ogerardin.xplane.aircraft.custom.ZiboMod738.GoogleDriveUpdateProvider.class, provider);
        assertSame(provider, aircraft.getUpdateProvider().orElseThrow());
        assertFalse(aircraft.isUpdateLocked());
    }

    @Test
    void lockedSkunkcraftsDoesNotFallBackToWebsite() throws Exception {
        var binary = root.resolve("Resources/plugins/Test/64/test.xpl");
        Files.createDirectories(binary.getParent());
        Files.writeString(binary, "");
        Files.writeString(binary.getParent().getParent().resolve("skunkcrafts_updater.cfg"),
                "version|1.0\nmodule|https://example.com\nlocked|true\n");
        var plugin = new XPlaneOrgPlugin(new XPlane(root), binary, "Test", "Test", "https://example.com/page");
        assertInstanceOf(Updatable.class, plugin);
        assertInstanceOf(SkunkcraftsUpdateProvider.class, plugin.getUpdateProvider().orElseThrow());
        assertEquals(UpdateStatus.State.LOCKED, plugin.getUpdateStatus().state());
        assertTrue(plugin.getUpdateStatus().offers().isEmpty());
    }

    @Test
    void unsupportedItemHasNoProviderAndWebsiteItemHasPublicationProvider() throws Exception {
        var binary = root.resolve("Resources/plugins/Test/64/test.xpl");
        Files.createDirectories(binary.getParent());
        Files.writeString(binary, "");
        assertTrue(new Plugin(new XPlane(root), binary).getUpdateProvider().isEmpty());
        var plugin = new XPlaneOrgPlugin(new XPlane(root), binary, "Test", "Test", "https://example.com/page");
        assertInstanceOf(PublicationUpdateProvider.class, plugin.getUpdateProvider().orElseThrow());
    }

    @Test
    void aircraftUsesSkunkcraftsOnlyWhenConfigured() throws Exception {
        var folder = root.resolve("Aircraft/A320");
        Files.createDirectories(folder);
        var acf = folder.resolve("test.acf");
        Files.writeString(acf, """
                I
                1200 version
                ACF
                PROPERTIES_BEGIN
                P acf/_name Test
                PROPERTIES_END
                """);
        var aircraft = new com.ogerardin.xplane.aircraft.Aircraft(new XPlane(root),
                new com.ogerardin.xplane.file.AcfFile(acf));
        assertTrue(aircraft.getUpdateProvider().isEmpty());
        Files.writeString(folder.resolve("skunkcrafts_updater.cfg"),
                "version|1.0\nmodule|https://example.com\n");
        var reloaded = new com.ogerardin.xplane.aircraft.Aircraft(new XPlane(root),
                new com.ogerardin.xplane.file.AcfFile(acf));
        assertInstanceOf(SkunkcraftsUpdateProvider.class, reloaded.getUpdateProvider().orElseThrow());
    }

    @Test
    void configurationBackedVersionsDoNotRecurse() throws Exception {
        var folder = root.resolve("Resources/plugins/openSAM");
        Files.createDirectories(folder.resolve("64"));
        Files.writeString(folder.resolve("64/openSAM.xpl"), "");
        Files.writeString(folder.resolve("skunkcrafts_updater.cfg"), "version|5.4.1\nmodule|https://example.com\n");
        var plugin = new OpenSamPlugin(new XPlane(root), folder.resolve("64/openSAM.xpl"));
        assertEquals("5.4.1", plugin.getVersion());
        assertEquals("5.4.1", plugin.getInstalledVersion());
        var scenery = new SceneryPackage(folder);
        assertEquals("5.4.1", scenery.getVersion());
        assertEquals("5.4.1", scenery.getInstalledVersion());
    }

    @Test
    void enablingAnAlreadyLoadedPluginUpdatesItsCurrentFolder() throws Exception {
        try (var server = com.ogerardin.xplane.test.skunkcrafts.SkunkcraftsServerFixture.start()) {
            var binary = root.resolve("Resources/plugins (disabled)/Test/64/test.xpl");
            Files.createDirectories(binary.getParent());
            Files.writeString(binary, "");
            Files.writeString(binary.getParent().getParent().resolve("skunkcrafts_updater.cfg"),
                    "module|" + server.baseUrl() + "\nversion|1.0\n");
            server.serve("skunkcrafts_updater.cfg", "version|2.0\n")
                    .serve("skunkcrafts_updater_whitelist.txt", "updated.txt||3\n").serve("updated.txt", "new");
            var plugin = new Plugin(new XPlane(root), binary);
            var provider = plugin.getUpdateProvider().orElseThrow();
            assertEquals(UpdateStatus.State.DISABLED, provider.getStatus().state());
            plugin.setEnabled(true);
            provider.checkForUpdates();
            ((UpdateAction.Direct) provider.getStatus().offers().getFirst().action()).apply((p, m) -> {});
            assertEquals("new", Files.readString(root.resolve("Resources/plugins/Test/updated.txt")));
            assertFalse(Files.exists(binary.getParent().getParent()));
        }
    }

    @Test
    void sceneryKeepsItsDomainVersionWhenUpdaterConfigHasNoVersion() throws Exception {
        var folder = root.resolve("Custom Scenery/OpenSceneryX");
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("version.txt"), "4.4.0\n");
        Files.writeString(folder.resolve("skunkcrafts_updater.cfg"), "module|https://example.com\n");
        var scenery = new com.ogerardin.xplane.scenery.custom.OpenSceneryX(folder);
        assertEquals("4.4.0", scenery.getVersion());
        assertEquals("4.4.0", scenery.getInstalledVersion());
    }
}
