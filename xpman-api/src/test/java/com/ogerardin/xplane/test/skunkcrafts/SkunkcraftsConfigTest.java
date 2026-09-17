package com.ogerardin.xplane.test.skunkcrafts;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SkunkcraftsConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void testParseValidConfig() throws IOException {
        Path cfgFile = tempDir.resolve("skunkcrafts_updater.cfg");
        Files.writeString(cfgFile, """
                zone|custom
                module|https://example.com/addon
                name|Test Addon
                version|1.2.3
                locked|false
                disabled|false
                liveries|true
                """);

        SkunkcraftsConfig config = SkunkcraftsConfig.parse(cfgFile);

        assertThat(config.zone(), is("custom"));
        assertThat(config.moduleUrl(), is("https://example.com/addon"));
        assertThat(config.name(), is("Test Addon"));
        assertThat(config.version(), is("1.2.3"));
        assertThat(config.locked(), is(false));
        assertThat(config.disabled(), is(false));
        assertThat(config.liveries(), is(true));
    }

    @Test
    void testParseConfigWithComments() throws IOException {
        Path cfgFile = tempDir.resolve("skunkcrafts_updater.cfg");
        Files.writeString(cfgFile, """
                # This is a comment
                zone|custom
                module|https://example.com/addon
                # Another comment
                name|Test Addon
                version|1.0.0
                """);

        SkunkcraftsConfig config = SkunkcraftsConfig.parse(cfgFile);

        assertThat(config.zone(), is("custom"));
        assertThat(config.moduleUrl(), is("https://example.com/addon"));
        assertThat(config.name(), is("Test Addon"));
        assertThat(config.version(), is("1.0.0"));
    }

    @Test
    void testParseConfigWithMissingFields() throws IOException {
        Path cfgFile = tempDir.resolve("skunkcrafts_updater.cfg");
        Files.writeString(cfgFile, """
                module|https://example.com/addon
                """);

        SkunkcraftsConfig config = SkunkcraftsConfig.parse(cfgFile);

        assertThat(config.moduleUrl(), is("https://example.com/addon"));
        assertThat(config.name(), is(nullValue()));
        assertThat(config.version(), is(nullValue()));
        assertThat(config.zone(), is(nullValue()));
        assertThat(config.locked(), is(false));
        assertThat(config.disabled(), is(false));
        assertThat(config.liveries(), is(true));
    }

    @Test
    void testParseConfigWithLockedTrue() throws IOException {
        Path cfgFile = tempDir.resolve("skunkcrafts_updater.cfg");
        Files.writeString(cfgFile, """
                module|https://example.com/addon
                locked|true
                """);

        SkunkcraftsConfig config = SkunkcraftsConfig.parse(cfgFile);

        assertThat(config.locked(), is(true));
    }

    @Test
    void testParseConfigWithDisabledTrue() throws IOException {
        Path cfgFile = tempDir.resolve("skunkcrafts_updater.cfg");
        Files.writeString(cfgFile, """
                module|https://example.com/addon
                disabled|true
                """);

        SkunkcraftsConfig config = SkunkcraftsConfig.parse(cfgFile);

        assertThat(config.disabled(), is(true));
    }

    @Test
    void testParseConfigWithLiveriesFalse() throws IOException {
        Path cfgFile = tempDir.resolve("skunkcrafts_updater.cfg");
        Files.writeString(cfgFile, """
                module|https://example.com/addon
                liveries|false
                """);

        SkunkcraftsConfig config = SkunkcraftsConfig.parse(cfgFile);

        assertThat(config.liveries(), is(false));
    }

    @Test
    void testParseConfigWithEmptyLines() throws IOException {
        Path cfgFile = tempDir.resolve("skunkcrafts_updater.cfg");
        Files.writeString(cfgFile, """
                
                module|https://example.com/addon
                
                name|Test
                
                """);

        SkunkcraftsConfig config = SkunkcraftsConfig.parse(cfgFile);

        assertThat(config.moduleUrl(), is("https://example.com/addon"));
        assertThat(config.name(), is("Test"));
    }

    @Test
    void testParseConfigWithMixedCaseKeys() throws IOException {
        Path cfgFile = tempDir.resolve("skunkcrafts_updater.cfg");
        Files.writeString(cfgFile, """
                MODULE|https://example.com/addon
                Name|Test Addon
                VERSION|2.0.0
                """);

        SkunkcraftsConfig config = SkunkcraftsConfig.parse(cfgFile);

        assertThat(config.moduleUrl(), is("https://example.com/addon"));
        assertThat(config.name(), is("Test Addon"));
        assertThat(config.version(), is("2.0.0"));
    }
}
