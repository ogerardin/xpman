package com.ogerardin.xplane.test.util;

import com.ogerardin.xplane.util.platform.Platforms;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Version extraction from raw binaries via {@code Platform.extractVersion}.
 */
class PlatformTest {

    // the pattern declared by skunkcrafts-updater.json: a version-named Go source file embedded as a string
    // constant, excluding merged string-blob artifacts (preceded by digit/dot) and NUL-terminated path entries
    private static final Pattern SKUNKCRAFTS_VERSION_PATTERN =
            Pattern.compile("(?<![\\d.])(\\d+\\.\\d+[a-z]?)\\.go(?!\\x00)");

    @Test
    void extractVersionFindsVersionNamedSourceFileAmongDecoys() throws IOException {
        Path binary = Files.createTempFile("tool", ".bin");
        try {
            // mimics the real SkunkcraftsUpdater binary: a merged sorted-string blob ("93.2e.go"),
            // a NUL-terminated vendored Go source path, then the app's own string constant ("Plugins3.2e.go")
            String content = "2.5.4.93.2e.go9765625"
                    + "/usr/local/go/src/internal/poly1305/bits_go1.13.go\u0000"
                    + "Plugins3.2e.goversionenabled";
            Files.write(binary, content.getBytes(StandardCharsets.ISO_8859_1));
            assertThat(Platforms.getCurrent().extractVersion(binary, SKUNKCRAFTS_VERSION_PATTERN), is("3.2e"));
        } finally {
            Files.deleteIfExists(binary);
        }
    }

    @Test
    void extractVersionYieldsNullWhenPatternAbsent() throws IOException {
        Path binary = Files.createTempFile("tool", ".bin");
        try {
            Files.write(binary, "nothing to see here".getBytes(StandardCharsets.ISO_8859_1));
            assertThat(Platforms.getCurrent().extractVersion(binary, SKUNKCRAFTS_VERSION_PATTERN), nullValue());
        } finally {
            Files.deleteIfExists(binary);
        }
    }
}
