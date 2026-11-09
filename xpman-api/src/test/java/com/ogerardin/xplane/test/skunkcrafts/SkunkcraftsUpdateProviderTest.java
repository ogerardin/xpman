package com.ogerardin.xplane.test.skunkcrafts;

import com.ogerardin.xplane.skunkcrafts.*;
import com.ogerardin.xplane.update.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

/** Generic offers retain Skunkcrafts content truth and execution safeguards. */
class SkunkcraftsUpdateProviderTest {
    @TempDir Path folder;

    @Test
    void genericChannelsSelectTheirOwnSummariesAndExecution() throws Exception {
        try (var server = SkunkcraftsServerFixture.start()) {
            server.serve("release/skunkcrafts_updater.cfg", "version|2.0\n")
                    .serve("release/skunkcrafts_updater_whitelist.txt", "release.txt||3\n")
                    .serve("beta/skunkcrafts_updater.cfg", "version|3.0-beta\n")
                    .serve("beta/skunkcrafts_updater_whitelist.txt", "beta.txt||3\nextra.txt||3\n")
                    .serve("beta/beta.txt", "new").serve("beta/extra.txt", "new");
            Files.writeString(folder.resolve("skunkcrafts_updater.cfg"), "version|1.0\nmodule|" + server.baseUrl() + "release/\n");
            var provider = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl() + "release/"),
                    config(server.baseUrl() + "beta/"), () -> "1.0", () -> true);
            provider.checkForUpdates();
            assertEquals(1, provider.getUpdateSummary(UpdateChannel.RELEASE).fileCount());
            assertEquals(2, provider.getUpdateSummary(UpdateChannel.BETA).fileCount());
            var beta = provider.getStatus().offers().stream().filter(o -> o.channel() == UpdateChannel.BETA).findFirst().orElseThrow();
            ((UpdateAction.Direct) beta.action()).apply((p, m) -> {});
            assertEquals("new", Files.readString(folder.resolve("beta.txt")));
            assertFalse(Files.exists(folder.resolve("release.txt")));
        }
    }

    @Test
    void contentChecksAndDirectActionsHonorLiveEligibility() throws Exception {
        try (var server = SkunkcraftsServerFixture.start()) {
            server.serve("skunkcrafts_updater.cfg", "version|2.0\n")
                    .serve("skunkcrafts_updater_whitelist.txt", "file.txt||3\n")
                    .serve("file.txt", "new");
            var enabled = new AtomicBoolean(true);
            Files.writeString(folder.resolve("skunkcrafts_updater.cfg"), "module|" + server.baseUrl() + "\nversion|1.0\n");
            var provider = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl()), null, () -> "99", enabled::get);
            assertEquals(UpdateStatus.State.UNKNOWN, provider.getStatus().state());
            provider.checkForUpdates();
            assertEquals("1.0", provider.getStatus().installedVersion());
            assertEquals(UpdateStatus.State.AVAILABLE, provider.getStatus().state());
            var action = (UpdateAction.Direct) provider.getStatus().offers().getFirst().action();
            enabled.set(false);
            assertTrue(provider.getStatus().offers().isEmpty());
            assertThrows(IOException.class, () -> action.apply((p, m) -> {}));
            assertFalse(Files.exists(folder.resolve("file.txt")));
            enabled.set(true);
            action.apply((p, m) -> {});
            assertEquals("new", Files.readString(folder.resolve("file.txt")));
        }
    }

    @Test
    void labelMismatchAloneDoesNotCreateOfferAndFailedDiscoveryIsNotCurrent() throws Exception {
        try (var server = SkunkcraftsServerFixture.start()) {
            Files.writeString(folder.resolve("file.txt"), "old");
            server.serve("skunkcrafts_updater.cfg", "version|2.0\n")
                    .serve("skunkcrafts_updater_whitelist.txt", "file.txt||3\n");
            var provider = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl()), null, () -> "99", () -> true);
            provider.checkForUpdates();
            assertEquals(UpdateStatus.State.CURRENT, provider.getStatus().state());
            assertTrue(provider.getStatus().offers().isEmpty());
            var failed = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl() + "missing/"), null, () -> "99", () -> true);
            failed.checkForUpdates();
            assertEquals(UpdateStatus.State.FAILED, failed.getStatus().state());
        }
    }

    @Test
    void betaPreferenceFiltersCachedOffersAndPreventsStaleExecution() throws Exception {
        try (var server = SkunkcraftsServerFixture.start()) {
            server.serve("stable/skunkcrafts_updater.cfg", "version|1.0\n")
                    .serve("stable/skunkcrafts_updater_whitelist.txt", "")
                    .serve("beta/skunkcrafts_updater.cfg", "version|2.0-beta\n")
                    .serve("beta/skunkcrafts_updater_whitelist.txt", "beta.txt||3\n");
            var provider = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl() + "stable/"),
                    config(server.baseUrl() + "beta/"), () -> "1.0", () -> true);
            provider.checkForUpdates();
            assertEquals(1, provider.getStatus().offers().size());
            assertEquals(UpdateChannel.BETA, provider.getStatus().offers().getFirst().channel());
            var action = (UpdateAction.Direct) provider.getStatus().offers().getFirst().action();
            SkunkcraftsUpdater.ignoreBetaChannel.set(() -> true);
            assertTrue(provider.getStatus().offers().isEmpty());
            assertThrows(IOException.class, () -> action.apply((p, m) -> {}));
        } finally { SkunkcraftsUpdater.ignoreBetaChannel.set(() -> false); }
    }

    @Test
    void staleManifestChecksumKeepsDownloadedFileAndCompletesUpdate() throws Exception {
        try (var server = SkunkcraftsServerFixture.start()) {
            // file.txt carries a bogus CRC (12345), good.txt follows it in the whitelist:
            // the update must continue past the failed verification and install both.
            server.serve("skunkcrafts_updater.cfg", "version|2.0\n")
                    .serve("skunkcrafts_updater_whitelist.txt", "file.txt|12345|3\ngood.txt||4\n")
                    .serve("file.txt", "new")
                    .serve("good.txt", "good");
            Files.writeString(folder.resolve("skunkcrafts_updater.cfg"), "version|1.0\nmodule|" + server.baseUrl() + "\n");
            var provider = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl()), null, () -> "1.0", () -> true);
            provider.checkForUpdates();
            var action = (UpdateAction.Direct) provider.getStatus().offers().getFirst().action();
            var finalMessage = new String[1];
            action.apply((p, m) -> { if (p != null && p == 1.0) finalMessage[0] = m; });
            assertEquals("new", Files.readString(folder.resolve("file.txt")));
            assertEquals("good", Files.readString(folder.resolve("good.txt")));
            assertTrue(finalMessage[0].contains("could not be verified"));
            assertTrue(Files.readString(folder.resolve("skunkcrafts_updater.cfg")).contains("version|2.0"));
        }
    }

    @Test
    void staleManifestEntryIsRememberedAndResurfacesWhenRepublished() throws Exception {
        try (var server = SkunkcraftsServerFixture.start()) {
            server.serve("skunkcrafts_updater.cfg", "version|2.0\n")
                    .serve("skunkcrafts_updater_whitelist.txt", "file.txt|12345|3\n")
                    .serve("file.txt", "new");
            Files.writeString(folder.resolve("skunkcrafts_updater.cfg"), "version|1.0\nmodule|" + server.baseUrl() + "\n");
            updateViaNewProvider(server);
            assertEquals("new", Files.readString(folder.resolve("file.txt")));
            assertTrue(Files.exists(folder.resolve("skunkcrafts_updater_xpman.json")), "stale-entry state file missing");

            // Fresh provider = next XPman launch: the proven-stale entry is remembered and the served
            // file revalidated (304), so no phantom update is offered despite the stale manifest.
            var fresh = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl()), null, () -> "2.0", () -> true);
            fresh.checkForUpdates();
            assertTrue(fresh.getStatus().offers().isEmpty(), "phantom update offered after completed update");

            // The developer republishes the file (still without fixing the manifest): the revalidation
            // sees new bytes, the update resurfaces, and afterwards the record is refreshed (clean again).
            server.serve("file.txt", "newer");
            var afterRepublish = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl()), null, () -> "2.0", () -> true);
            afterRepublish.checkForUpdates();
            assertEquals(1, afterRepublish.getStatus().offers().size());
            updateViaNewProvider(server);
            assertEquals("newer", Files.readString(folder.resolve("file.txt")));

            var clean = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl()), null, () -> "2.0", () -> true);
            clean.checkForUpdates();
            assertTrue(clean.getStatus().offers().isEmpty(), "phantom update offered after republished update");

            // The developer finally fixes the file list: the update applies, verifies OK,
            // and the stale proof is forgotten — the state file cleans itself up.
            server.serve("file.txt", "newest");
            server.serve("skunkcrafts_updater_whitelist.txt", "file.txt|" + crcOf("newest") + "|6\n");
            var afterFix = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl()), null, () -> "2.0", () -> true);
            afterFix.checkForUpdates();
            assertEquals(1, afterFix.getStatus().offers().size());
            updateViaNewProvider(server);
            assertEquals("newest", Files.readString(folder.resolve("file.txt")));
            assertFalse(Files.exists(folder.resolve("skunkcrafts_updater_xpman.json")), "state file not cleaned up");
        }
    }

    private static long crcOf(String content) {
        var crc = new java.util.zip.CRC32();
        crc.update(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return crc.getValue();
    }

    /** Applies the pending update through a provider built from the current folder state. */
    private void updateViaNewProvider(SkunkcraftsServerFixture server) throws Exception {
        var provider = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl()), null, () -> "1.0", () -> true);
        provider.checkForUpdates();
        var action = (UpdateAction.Direct) provider.getStatus().offers().getFirst().action();
        action.apply((p, m) -> {});
    }

    private SkunkcraftsConfig config(String url) {
        return new SkunkcraftsConfig("Addon", "1.0", url, null, false, false, true);
    }

    @Test
    void unavailableBetaDoesNotHideStableOffer() throws Exception {
        try (var server = SkunkcraftsServerFixture.start()) {
            server.serve("stable/skunkcrafts_updater.cfg", "version|2.0\n")
                    .serve("stable/skunkcrafts_updater_whitelist.txt", "stable.txt||3\n");
            var provider = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl() + "stable/"),
                    config(server.baseUrl() + "dead-beta/"), () -> "1.0", () -> true);
            provider.checkForUpdates();
            assertEquals(UpdateStatus.State.AVAILABLE, provider.getStatus().state());
            assertEquals(UpdateChannel.RELEASE, provider.getStatus().offers().getFirst().channel());
            assertNotNull(provider.getStatus().message());
        }
    }

    @Test
    void manifestFailureAfterDiscoveryFailsReviewAndExecution() throws Exception {
        try (var server = SkunkcraftsServerFixture.start()) {
            server.serve("skunkcrafts_updater.cfg", "version|2.0\n")
                    .serve("skunkcrafts_updater_whitelist.txt", "file.txt||3\n").serve("file.txt", "new");
            Files.writeString(folder.resolve("skunkcrafts_updater.cfg"), "version|1.0\nmodule|" + server.baseUrl() + "\n");
            var provider = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl()), null, () -> "1.0", () -> true);
            provider.checkForUpdates();
            var action = (UpdateAction.Direct) provider.getStatus().offers().getFirst().action();
            assertEquals(1, provider.getSkunkcraftsUpdateSummary().fileCount());
            server.remove("skunkcrafts_updater_whitelist.txt");
            assertThrows(IOException.class, provider::getSkunkcraftsUpdateSummary);
            var completed = new AtomicBoolean();
            assertThrows(IOException.class, () -> action.apply((p, m) -> {
                if (p != null && p == 1.0) completed.set(true);
            }));
            assertFalse(completed.get());
            assertFalse(Files.exists(folder.resolve("file.txt")));
        }
    }

    @Test
    void failedVersionRequestIsNotCurrentButDoesNotHideChecksumOffers() throws Exception {
        try (var server = SkunkcraftsServerFixture.start()) {
            server.serve("skunkcrafts_updater_whitelist.txt", "");
            var provider = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl()), null, () -> "1.0", () -> true);
            provider.checkForUpdates();
            assertEquals(UpdateStatus.State.FAILED, provider.getStatus().state());
            assertNotNull(provider.getStatus().message());
            server.serve("skunkcrafts_updater_whitelist.txt", "file.txt||3\n");
            provider.checkForUpdates();
            assertEquals(UpdateStatus.State.AVAILABLE, provider.getStatus().state());
            assertEquals(1, provider.getStatus().offers().size());
            assertNull(provider.getStatus().latestVersion());
            assertNotNull(provider.getStatus().message());
        }
    }

    @Test
    void failedBetaVersionRequestPreservesBetaContentOffer() throws Exception {
        try (var server = SkunkcraftsServerFixture.start()) {
            server.serve("stable/skunkcrafts_updater.cfg", "version|1.0\n")
                    .serve("stable/skunkcrafts_updater_whitelist.txt", "")
                    .serve("beta/skunkcrafts_updater_whitelist.txt", "beta.txt||3\n");
            var provider = new SkunkcraftsUpdateProvider(folder, config(server.baseUrl() + "stable/"),
                    config(server.baseUrl() + "beta/"), () -> "1.0", () -> true);
            provider.checkForUpdates();
            assertEquals(UpdateStatus.State.AVAILABLE, provider.getStatus().state());
            assertEquals(UpdateChannel.BETA, provider.getStatus().offers().getFirst().channel());
            assertNull(provider.getStatus().offers().getFirst().version());
            assertNotNull(provider.getStatus().message());
        }
    }
}
