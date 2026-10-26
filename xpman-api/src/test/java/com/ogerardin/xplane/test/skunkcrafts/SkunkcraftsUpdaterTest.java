package com.ogerardin.xplane.test.skunkcrafts;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsChannelStatus;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdater;
import com.ogerardin.xplane.skunkcrafts.WhitelistEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SkunkcraftsUpdaterTest {

    @TempDir
    Path tempDir;

    @Test
    void testFindConfigReturnsNullWhenNoConfig() {
        SkunkcraftsConfig config = SkunkcraftsUpdater.findConfig(tempDir);
        assertThat(config, is(nullValue()));
    }

    @Test
    void testFindConfigReturnsConfigWhenPresent() throws IOException {
        Path cfgFile = tempDir.resolve("skunkcrafts_updater.cfg");
        Files.writeString(cfgFile, """
                module|https://example.com/addon
                name|Test
                version|1.0.0
                """);

        SkunkcraftsConfig config = SkunkcraftsUpdater.findConfig(tempDir);

        assertThat(config, is(notNullValue()));
        assertThat(config.moduleUrl(), is("https://example.com/addon"));
        assertThat(config.name(), is("Test"));
    }

    @Test
    void testParseWhitelist() {
        String content = """
                file1.txt|12345678|100
                subdir/file2.obj|999999|200
                file3.png|0|300
                """;

        List<WhitelistEntry> entries = SkunkcraftsUpdater.parseWhitelist(content);

        assertThat(entries, hasSize(3));
        assertThat(entries.get(0).relativePath(), is("file1.txt"));
        assertThat(entries.get(0).expectedCRC(), is(12345678L));
        assertThat(entries.get(0).expectedSize(), is(100L));
        assertThat(entries.get(1).relativePath(), is("subdir/file2.obj"));
        assertThat(entries.get(1).expectedCRC(), is(999999L));
        assertThat(entries.get(2).relativePath(), is("file3.png"));
        assertThat(entries.get(2).expectedCRC(), is(0L));
    }

    @Test
    void testParseWhitelistSkipsMetadataKeys() {
        String content = """
                name|Test Addon
                version|1.0.0
                module|https://example.com
                file1.txt|12345678|100
                """;

        List<WhitelistEntry> entries = SkunkcraftsUpdater.parseWhitelist(content);

        assertThat(entries, hasSize(1));
        assertThat(entries.get(0).relativePath(), is("file1.txt"));
    }

    @Test
    void testParseWhitelistSkipsComments() {
        String content = """
                # This is a comment
                file1.txt|12345678|100
                ; Another comment
                file2.txt|abcdef00|200
                """;

        List<WhitelistEntry> entries = SkunkcraftsUpdater.parseWhitelist(content);

        assertThat(entries, hasSize(2));
    }

    @Test
    void testParseWhitelistHandlesPathsWithBackslashes() {
        String content = "subdir\\file.txt|12345678|100";

        List<WhitelistEntry> entries = SkunkcraftsUpdater.parseWhitelist(content);

        assertThat(entries, hasSize(1));
        assertThat(entries.get(0).relativePath(), is("subdir/file.txt"));
    }

    @Test
    void testParseBlacklist() {
        String content = """
                user_config.txt
                cache/
                *.log
                """;

        Set<String> paths = SkunkcraftsUpdater.parseBlacklist(content);

        assertThat(paths, hasSize(3));
        assertThat(paths, hasItems("user_config.txt", "cache", "*.log"));
    }

    @Test
    void testParseBlacklistSkipsComments() {
        String content = """
                # Comment
                file1.txt
                ; Another comment
                file2.txt
                """;

        Set<String> paths = SkunkcraftsUpdater.parseBlacklist(content);

        assertThat(paths, hasSize(2));
        assertThat(paths, hasItems("file1.txt", "file2.txt"));
    }

    @Test
    void testIsIgnoredExactMatch() {
        Set<String> ignoreSet = Set.of("user_config.txt");

        assertThat(SkunkcraftsUpdater.isIgnored("user_config.txt", ignoreSet), is(true));
        assertThat(SkunkcraftsUpdater.isIgnored("other_file.txt", ignoreSet), is(false));
    }

    @Test
    void testIsIgnoredPrefixMatch() {
        Set<String> ignoreSet = Set.of("cache");

        assertThat(SkunkcraftsUpdater.isIgnored("cache/file.txt", ignoreSet), is(true));
        assertThat(SkunkcraftsUpdater.isIgnored("cache/subdir/file.txt", ignoreSet), is(true));
        assertThat(SkunkcraftsUpdater.isIgnored("other/file.txt", ignoreSet), is(false));
    }

    @Test
    void testIsIgnoredWildcardMatch() {
        Set<String> ignoreSet = Set.of("*.log");

        assertThat(SkunkcraftsUpdater.isIgnored("debug.log", ignoreSet), is(true));
        assertThat(SkunkcraftsUpdater.isIgnored("subdir/error.log", ignoreSet), is(true));
        assertThat(SkunkcraftsUpdater.isIgnored("file.txt", ignoreSet), is(false));
    }

    @Test
    void testComputeCRC32() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Hello, World!");

        long crc = SkunkcraftsUpdater.computeCRC32(testFile);

        assertThat(crc, is(not(0L)));
    }

    @Test
    void testComputeFilesToUpdateMissingFile() throws IOException {
        List<WhitelistEntry> whitelist = List.of(
                new WhitelistEntry("missing.txt", 12345678L, 100L)
        );
        Set<String> ignoreSet = new HashSet<>();

        List<WhitelistEntry> toUpdate = SkunkcraftsUpdater.computeFilesToUpdate(tempDir, whitelist, ignoreSet);

        assertThat(toUpdate, hasSize(1));
        assertThat(toUpdate.get(0).relativePath(), is("missing.txt"));
    }

    @Test
    void testComputeFilesToUpdateCRCMismatch() throws IOException {
        Path existingFile = tempDir.resolve("existing.txt");
        Files.writeString(existingFile, "content");

        List<WhitelistEntry> whitelist = List.of(
                new WhitelistEntry("existing.txt", 99999999L, null)
        );
        Set<String> ignoreSet = new HashSet<>();

        List<WhitelistEntry> toUpdate = SkunkcraftsUpdater.computeFilesToUpdate(tempDir, whitelist, ignoreSet);

        assertThat(toUpdate, hasSize(1));
    }

    @Test
    void testComputeFilesToUpdateSizeMismatch() throws IOException {
        Path existingFile = tempDir.resolve("existing.txt");
        Files.writeString(existingFile, "short");

        List<WhitelistEntry> whitelist = List.of(
                new WhitelistEntry("existing.txt", null, 999999L)
        );
        Set<String> ignoreSet = new HashSet<>();

        List<WhitelistEntry> toUpdate = SkunkcraftsUpdater.computeFilesToUpdate(tempDir, whitelist, ignoreSet);

        assertThat(toUpdate, hasSize(1));
    }

    @Test
    void testComputeFilesToUpdateNoChangesNeeded() throws IOException {
        Path existingFile = tempDir.resolve("existing.txt");
        String content = "test content";
        Files.writeString(existingFile, content);

        long actualCRC = SkunkcraftsUpdater.computeCRC32(existingFile);
        long actualSize = Files.size(existingFile);

        List<WhitelistEntry> whitelist = List.of(
                new WhitelistEntry("existing.txt", actualCRC, actualSize)
        );
        Set<String> ignoreSet = new HashSet<>();

        List<WhitelistEntry> toUpdate = SkunkcraftsUpdater.computeFilesToUpdate(tempDir, whitelist, ignoreSet);

        assertThat(toUpdate, is(empty()));
    }

    @Test
    void testComputeFilesToUpdateRespectsIgnoreSet() throws IOException {
        List<WhitelistEntry> whitelist = List.of(
                new WhitelistEntry("normal.txt", 12345678L, 100L),
                new WhitelistEntry("ignored.txt", 87654321L, 200L)
        );
        Set<String> ignoreSet = Set.of("ignored.txt");

        List<WhitelistEntry> toUpdate = SkunkcraftsUpdater.computeFilesToUpdate(tempDir, whitelist, ignoreSet);

        assertThat(toUpdate, hasSize(1));
        assertThat(toUpdate.get(0).relativePath(), is("normal.txt"));
    }

    @Test
    void testComputeFilesToUpdateSkipsSentinelCRC() throws IOException {
        Path existingFile = tempDir.resolve("existing.txt");
        Files.writeString(existingFile, "content");

        List<WhitelistEntry> whitelist = List.of(
                new WhitelistEntry("existing.txt", 0xFFFFFFFFL, null),
                new WhitelistEntry("existing.txt", 0L, null)
        );
        Set<String> ignoreSet = new HashSet<>();

        List<WhitelistEntry> toUpdate = SkunkcraftsUpdater.computeFilesToUpdate(tempDir, whitelist, ignoreSet);

        assertThat(toUpdate, is(empty()));
    }

    @Test
    void testIsLockedReturnsFalseWhenNoConfig() {
        assertThat(SkunkcraftsUpdater.isLocked(tempDir), is(false));
    }

    @Test
    void testIsLockedReturnsTrueWhenLocked() throws IOException {
        Path cfgFile = tempDir.resolve("skunkcrafts_updater.cfg");
        Files.writeString(cfgFile, """
                module|https://example.com/addon
                locked|true
                """);

        assertThat(SkunkcraftsUpdater.isLocked(tempDir), is(true));
    }

    @Test
    void testIsLockedReturnsFalseWhenNotLocked() throws IOException {
        Path cfgFile = tempDir.resolve("skunkcrafts_updater.cfg");
        Files.writeString(cfgFile, """
                module|https://example.com/addon
                locked|false
                """);

        assertThat(SkunkcraftsUpdater.isLocked(tempDir), is(false));
    }

    @Test
    void testParseWhitelistNormalizesCRC32To32Bits() {
        // Test that 64-bit CRC values are normalized to 32 bits
        String content = """
                file1.txt|FFFFFFFFFFFFFFFF|100
                file2.txt|0xFFFFFFFF|200
                file3.txt|123456789ABCDEF0|300
                """;

        List<WhitelistEntry> entries = SkunkcraftsUpdater.parseWhitelist(content);

        assertThat(entries, hasSize(3));
        // FFFFFFFFFFFFFFFF should be normalized to FFFFFFFF (sentinel value)
        assertThat(entries.get(0).expectedCRC(), is(0xFFFFFFFFL));
        // 0xFFFFFFFF should remain FFFFFFFF
        assertThat(entries.get(1).expectedCRC(), is(0xFFFFFFFFL));
        // 123456789ABCDEF0 should be normalized to 9ABCDEF0 (lower 32 bits)
        assertThat(entries.get(2).expectedCRC(), is(0x9ABCDEF0L));
    }

    @Test
    void testEncodeUrlEncodesSpacesInFilename() throws IOException {
        // Reproduces the SGES update failure: whitelist paths may contain spaces (e.g. "SGES_history (4).pdf")
        java.net.URL url = SkunkcraftsUpdater.encodeUrl(
                "https://raw.githubusercontent.com/GitHubJavelin/sges-updates/refs/heads/main/Scripts/SGES_history (4).pdf");

        assertThat(url.toString(), is(
                "https://raw.githubusercontent.com/GitHubJavelin/sges-updates/refs/heads/main/Scripts/SGES_history%20(4).pdf"));
    }

    @Test
    void testEncodeUrlLeavesPlainUrlsUnchanged() throws IOException {
        java.net.URL url = SkunkcraftsUpdater.encodeUrl("https://example.com/skunkcrafts_updater.cfg");

        assertThat(url.toString(), is("https://example.com/skunkcrafts_updater.cfg"));
    }

    @Test
    void findBetaConfigReturnsNullWhenAbsent() {
        assertThat(SkunkcraftsUpdater.findBetaConfig(tempDir), is(nullValue()));
    }

    @Test
    void findBetaConfigReturnsConfigWhenPresent() throws IOException {
        Files.writeString(tempDir.resolve("skunkcrafts_updater_beta.cfg"), """
                zone|custom
                module|https://example.com/sges-updates-beta
                name|Simple Ground Equip. & Services
                version|Beta
                locked|false
                """);

        SkunkcraftsConfig config = SkunkcraftsUpdater.findBetaConfig(tempDir);

        assertThat(config, is(notNullValue()));
        assertThat(config.moduleUrl(), is("https://example.com/sges-updates-beta"));
        assertThat(config.version(), is("Beta"));
    }

    @Test
    void computeChannelStatusReportsStableOnlyWhenNoBetaCfg() throws IOException {
        try (SkunkcraftsServerFixture server = SkunkcraftsServerFixture.start()) {
            Files.writeString(tempDir.resolve("file1.txt"), "old");
            server.serve("stable/skunkcrafts_updater.cfg", remoteCfg("2.0", server.baseUrl() + "stable"));
            server.serve("stable/skunkcrafts_updater_whitelist.txt", "file1.txt|" + crcOf("new") + "|99\n");

            SkunkcraftsChannelStatus status = SkunkcraftsUpdater.computeChannelStatus(
                    tempDir, localCfg(server.baseUrl() + "stable"), null);

            assertThat(status.stableUpdateAvailable(), is(true));
            assertThat(status.betaUpdateAvailable(), is(false));
            assertThat(status.betaLatestVersion(), is(nullValue()));
        }
    }

    @Test
    void computeChannelStatusSuppressesBetaWhenWhitelistsIdentical() throws IOException {
        try (SkunkcraftsServerFixture server = SkunkcraftsServerFixture.start()) {
            Files.writeString(tempDir.resolve("file1.txt"), "old");
            String whitelist = "file1.txt|" + crcOf("new") + "|99\n";
            server.serve("stable/skunkcrafts_updater.cfg", remoteCfg("2.0", server.baseUrl() + "stable"));
            server.serve("stable/skunkcrafts_updater_whitelist.txt", whitelist);
            server.serve("beta/skunkcrafts_updater.cfg", remoteCfg("Beta", server.baseUrl() + "beta"));
            // beta whitelist identical to stable: the SGES case, suppressed regardless of labels
            server.serve("beta/skunkcrafts_updater_whitelist.txt", whitelist);

            SkunkcraftsChannelStatus status = SkunkcraftsUpdater.computeChannelStatus(
                    tempDir, localCfg(server.baseUrl() + "stable"), localCfg(server.baseUrl() + "beta"));

            assertThat(status.stableUpdateAvailable(), is(true));
            assertThat(status.betaUpdateAvailable(), is(false));
            assertThat(status.betaLatestVersion(), is(nullValue()));
        }
    }

    @Test
    void computeChannelStatusOffersBetaWhenContentDiffersAndLabelsAreEqual() throws IOException {
        try (SkunkcraftsServerFixture server = SkunkcraftsServerFixture.start()) {
            Files.writeString(tempDir.resolve("file1.txt"), "old");
            server.serve("stable/skunkcrafts_updater.cfg", remoteCfg("2.0", server.baseUrl() + "stable"));
            // local matches stable content but not beta content
            server.serve("stable/skunkcrafts_updater_whitelist.txt", "file1.txt|" + crcOf("old") + "|99\n");
            server.serve("beta/skunkcrafts_updater.cfg", remoteCfg("2.0", server.baseUrl() + "beta"));
            server.serve("beta/skunkcrafts_updater_whitelist.txt", "file1.txt|" + crcOf("beta content") + "|99\n");

            SkunkcraftsChannelStatus status = SkunkcraftsUpdater.computeChannelStatus(
                    tempDir, localCfg(server.baseUrl() + "stable"), localCfg(server.baseUrl() + "beta"));

            assertThat(status.stableUpdateAvailable(), is(false));
            assertThat(status.betaUpdateAvailable(), is(true));
            assertThat(status.betaLatestVersion(), is("2.0"));
        }
    }

    @Test
    void computeChannelStatusToleratesUnreachableWhitelist() throws IOException {
        try (SkunkcraftsServerFixture server = SkunkcraftsServerFixture.start()) {
            server.serve("stable/skunkcrafts_updater.cfg", remoteCfg("2.0", server.baseUrl() + "stable"));
            server.serve("beta/skunkcrafts_updater.cfg", remoteCfg("Beta", server.baseUrl() + "beta"));
            // no whitelists served

            SkunkcraftsChannelStatus status = SkunkcraftsUpdater.computeChannelStatus(
                    tempDir, localCfg(server.baseUrl() + "stable"), localCfg(server.baseUrl() + "beta"));

            assertThat(status.stableUpdateAvailable(), is(false));
            assertThat(status.betaUpdateAvailable(), is(false));
            assertThat(status.betaLatestVersion(), is(nullValue()));
        }
    }

    @Test
    void computeChannelStatusIgnoresDeadBetaModule() throws IOException {
        try (SkunkcraftsServerFixture server = SkunkcraftsServerFixture.start()) {
            Files.writeString(tempDir.resolve("file1.txt"), "old");
            server.serve("stable/skunkcrafts_updater.cfg", remoteCfg("2.0", server.baseUrl() + "stable"));
            server.serve("stable/skunkcrafts_updater_whitelist.txt", "file1.txt|" + crcOf("new") + "|99\n");
            // beta module URL serves nothing

            SkunkcraftsChannelStatus status = SkunkcraftsUpdater.computeChannelStatus(
                    tempDir, localCfg(server.baseUrl() + "stable"), localCfg(server.baseUrl() + "dead"));

            assertThat(status.stableUpdateAvailable(), is(true));
            assertThat(status.betaUpdateAvailable(), is(false));
            assertThat(status.betaLatestVersion(), is(nullValue()));
        }
    }

    private static SkunkcraftsConfig localCfg(String moduleUrl) {
        return new SkunkcraftsConfig("Test Addon", "1.0", moduleUrl, null, false, false, true);
    }

    /** A remote cfg body with the given version whose module points back at its own location. */
    private static String remoteCfg(String version, String moduleUrl) {
        return "name|Test Addon\nversion|" + version + "\nmodule|" + moduleUrl + "\ndisabled|false\nlocked|false\n";
    }

    private static long crcOf(String content) {
        CRC32 crc = new CRC32();
        crc.update(content.getBytes(StandardCharsets.UTF_8));
        return crc.getValue();
    }
}
