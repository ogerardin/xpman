package com.ogerardin.xplane.test.lua;

import com.ogerardin.xplane.plugins.custom.lua.LuaHeaderParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class LuaHeaderParserTest {

    @TempDir
    Path tempDir;

    @Test
    void parsesMetadataAndUsesFirstHeaderCommentAsFallbackDescription() throws IOException {
        Path script = tempDir.resolve("SimLoadManager.lua");
        Files.writeString(script, """
                -- General header text
                -- More header text
                -- @version 2.4
                -- @name Custom Name
                print('body')
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.name(), is("Custom Name"));
        assertThat(metadata.version(), is("2.4"));
        assertThat(metadata.description(), is("General header text"));
    }

    @Test
    void explicitDescriptionTakesPrecedenceOverGeneralComments() throws IOException {
        Path script = tempDir.resolve("test.lua");
        Files.writeString(script, """
                -- @description Explicit description
                -- General header text
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.description(), is("Explicit description"));
        assertThat(metadata.version(), is(nullValue()));
    }
}
