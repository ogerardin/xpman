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
    void explicitDescriptionReplacesFallbackAndStopsAtNextTag() throws IOException {
        Path script = tempDir.resolve("test.lua");
        Files.writeString(script, """
                -- General header text
                -- @description Explicit description
                -- @version 1.0
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.description(), is("Explicit description"));
        assertThat(metadata.version(), is("1.0"));
    }

    @Test
    void multiLineDescriptionCapturesContinuationLines() throws IOException {
        Path script = tempDir.resolve("multiline.lua");
        Files.writeString(script, """
                -- @description This is a long description
                -- that spans multiple lines
                -- and continues here
                -- @version 1.0
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.description(), is("This is a long description that spans multiple lines and continues here"));
        assertThat(metadata.version(), is("1.0"));
    }

    @Test
    void blockCommentDescriptionIsExtracted() throws IOException {
        Path script = tempDir.resolve("block.lua");
        Files.writeString(script, """
                --[[
                @description Description inside block comment
                @name Block Script
                @version 2.0
                ]]
                print('body')
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.name(), is("Block Script"));
        assertThat(metadata.description(), is("Description inside block comment"));
        assertThat(metadata.version(), is("2.0"));
    }

    @Test
    void singleLineBlockCommentIsExtracted() throws IOException {
        Path script = tempDir.resolve("singleblock.lua");
        Files.writeString(script, """
                --[[ @description Inline block description ]]
                --[[ @version 3.0 ]]
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.description(), is("Inline block description"));
        assertThat(metadata.version(), is("3.0"));
    }

    @Test
    void multiLineDescriptionStopsAtEmptyCommentLine() throws IOException {
        Path script = tempDir.resolve("paragraph.lua");
        Files.writeString(script, """
                -- @description First paragraph
                -- continues here
                --
                -- This is NOT part of description
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.description(), is("First paragraph continues here"));
    }

    @Test
    void decorationLinesAreFilteredFromFallbackDescription() throws IOException {
        Path script = tempDir.resolve("decorated.lua");
        Files.writeString(script, """
                -------------------------------
                --[[
                Example of use for XPLMFindCommand.
                  Some addons are creating custom commands/datarefs.
                --]]
                -------------------------------
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.description(), is("Example of use for XPLMFindCommand"));
    }

    @Test
    void trailingPeriodIsStrippedFromDescription() throws IOException {
        Path script = tempDir.resolve("period.lua");
        Files.writeString(script, """
                -- This script does something.
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.description(), is("This script does something"));
    }

    @Test
    void descriptionLabelWithContentOnNextLine() throws IOException {
        Path script = tempDir.resolve("gpu.lua");
        Files.writeString(script, """
                -- -- -- -- -- -- -- -- -- -- -- -- --
                -- Script:  ground power unit on/off
                -- Version: 1.0
                -- Build:   2012-10-20
                -- -- -- -- -- -- -- -- -- -- -- -- --
                -- Description:
                -- Provide a macro to turn on/off the GPU. And show if the GPU is connected.
                -- Usable in all planes, no matter if it has a GPU connector or not.
                -- -- -- -- -- -- -- -- -- -- -- -- --
                print('body')
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.description(), is(
                "Provide a macro to turn on/off the GPU. And show if the GPU is connected. " +
                "Usable in all planes, no matter if it has a GPU connector or not"));
        assertThat(metadata.version(), is("1.0"));
        assertThat(metadata.name(), is("gpu"));
    }

    @Test
    void trailingDecorativeDashesAreStripped() throws IOException {
        Path script = tempDir.resolve("decorated_dashes.lua");
        Files.writeString(script, """
                -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
                -- Script:  reload scenery                               --
                -- Version: 1.0                                          --
                -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
                -- Description:                                          --
                -- Will add a macro to reload the scenery.               --
                -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
                print('body')
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.description(), is("Will add a macro to reload the scenery"));
        assertThat(metadata.version(), is("1.0"));
    }

    @Test
    void blockCommentWithInnerCommentPrefixIsStripped() throws IOException {
        Path script = tempDir.resolve("block_inner_prefix.lua");
        Files.writeString(script, """
                --[[
                -- Place Aircraft At ICAO Airport Location test script
                --
                -- WARNING: place_aircraft_at reloads scenery and FlyWithLua!!
                --]]
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.description(), is(
                "Place Aircraft At ICAO Airport Location test script"));
    }

    @Test
    void versionExtractedFromVShorthand() throws IOException {
        Path script = tempDir.resolve("hasi.lua");
        Files.writeString(script, """
                -- Heading Altitude and Speed Instrument (HASI)
                -- V1.2 by Carsten Lynker
                -- build 2015-02-01
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.version(), is("1.2"));
        assertThat(metadata.description(), is("Heading Altitude and Speed Instrument (HASI)"));
    }

    @Test
    void versionExtractedFromVerShorthand() throws IOException {
        Path script = tempDir.resolve("imgui_show_plugin_info_in_vr.lua");
        Files.writeString(script, """
                -- Imugi Show Plugin Information In VR Ver. 1.02
                -- Using Imgui and ffi display plugin info and enable/disable in VR
                -- William R. Good 12-05-18
                """);

        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(script);

        assertThat(metadata.version(), is("1.02"));
        assertThat(metadata.description(), is("Imugi Show Plugin Information In VR Ver. 1.02"));
    }
}
