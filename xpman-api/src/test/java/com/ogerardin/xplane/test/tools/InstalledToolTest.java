package com.ogerardin.xplane.test.tools;

import com.ogerardin.xplane.tools.InstalledTool;
import com.ogerardin.xplane.tools.Manifest;
import com.ogerardin.xplane.util.platform.Platform;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Version resolution for installed tools: extraction is the only source of truth (manual installs / self-updates);
 * placeholder values from app metadata result in no version display (no manifest fallback).
 */
class InstalledToolTest {

    private static Manifest manifest(String version, Platform platform) {
        return manifest(version, platform, null);
    }

    private static Manifest manifest(String version, Platform platform, Pattern versionRegex) {
        return new Manifest("test", "Test", null, null, version, null, platform,
                null, null, null, null, null, null, versionRegex);
    }

    private static Platform platformReturning(String version) {
        Platform platform = mock(Platform.class);
        when(platform.getVersion(any())).thenReturn(version);
        return platform;
    }

    @Test
    void extractedRealVersionWinsOverManifest() {
        Manifest manifest = manifest("3.2e", platformReturning("3.3"));
        assertThat(new InstalledTool(Path.of("tool.app"), manifest).getVersion(), is("3.3"));
    }

    @Test
    void placeholderExtractedVersionYieldsNull() {
        Manifest manifest = manifest("3.2e", platformReturning("0.0.1"));
        assertThat(new InstalledTool(Path.of("tool.app"), manifest).getVersion(), nullValue());
    }

    @Test
    void absentExtractedVersionYieldsNull() {
        Manifest manifest = manifest("3.2e", platformReturning(null));
        assertThat(new InstalledTool(Path.of("tool.app"), manifest).getVersion(), nullValue());
    }

    @Test
    void placeholderExtractedVersionWithNoManifestVersionYieldsNull() {
        Manifest manifest = manifest(null, platformReturning("0.0.1"));
        assertThat(new InstalledTool(Path.of("tool.app"), manifest).getVersion(), nullValue());
    }

    @Test
    void blankExtractedVersionYieldsNull() {
        Manifest manifest = manifest("3.2e", platformReturning("  "));
        assertThat(new InstalledTool(Path.of("tool.app"), manifest).getVersion(), nullValue());
    }

    @Test
    void zeroZeroZeroPlaceholderYieldsNull() {
        Manifest manifest = manifest("1.0.0", platformReturning("0.0.0"));
        assertThat(new InstalledTool(Path.of("tool.app"), manifest).getVersion(), nullValue());
    }

    @Test
    void versionRegexTakesPrecedenceOverStandardExtraction() {
        Platform platform = mock(Platform.class);
        when(platform.getVersion(any())).thenReturn("0.0.1");
        when(platform.extractVersion(any(), any())).thenReturn("3.2e");
        Manifest manifest = manifest("3.2e", platform, Pattern.compile("v"));
        assertThat(new InstalledTool(Path.of("tool.app"), manifest).getVersion(), is("3.2e"));
    }

    @Test
    void versionRegexPlaceholderResultYieldsNull() {
        Platform platform = mock(Platform.class);
        when(platform.extractVersion(any(), any())).thenReturn("0.0.1");
        Manifest manifest = manifest("3.2e", platform, Pattern.compile("v"));
        assertThat(new InstalledTool(Path.of("tool.app"), manifest).getVersion(), nullValue());
    }

    @Test
    void noManifestYieldsNull() {
        Platform platform = mock(Platform.class);
        Mockito.lenient().when(platform.getVersion(any())).thenReturn("1.2.3");
        assertThat(new InstalledTool(Path.of("tool.app")).getVersion(), nullValue());
    }
}
