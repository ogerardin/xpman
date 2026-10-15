package com.ogerardin.xplane.test.file;

import com.ogerardin.xplane.file.AcfFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Hermetic tests for {@link AcfFile#save(Map)}: surgical write-back of changed properties
 * that preserves every other byte of the file.
 */
class AcfFileSaveTest {

    private static final String ACF_CONTENTS = """
            I
            1100 version
            ACF
            PROPERTIES_BEGIN
            P acf/_name Boeing 737-800
            P acf/_descrip Boeing 737-800
            P acf/_tailnum N737XL
            PROPERTIES_END
            """;

    @TempDir
    Path tempDir;

    private AcfFile writeAcf(String contents) throws IOException {
        Path acfPath = tempDir.resolve("test.acf");
        Files.writeString(acfPath, contents);
        return new AcfFile(acfPath);
    }

    @Test
    void saveChangesValueAndPreservesOtherLines() throws IOException {
        AcfFile acfFile = writeAcf(ACF_CONTENTS);

        acfFile.save(Map.of("acf/_descrip", "Boeing 737-900"));

        String saved = Files.readString(acfFile.getFile());
        assertThat(saved, containsString("P acf/_descrip Boeing 737-900"));
        // untouched lines preserved
        assertThat(saved, containsString("P acf/_name Boeing 737-800"));
        assertThat(saved, containsString("P acf/_tailnum N737XL"));
        assertThat(saved, containsString("PROPERTIES_BEGIN"));
        assertThat(saved, containsString("PROPERTIES_END"));
        // in-memory map updated
        assertThat(acfFile.getProperty("acf/_descrip"), is("Boeing 737-900"));
        assertThat(acfFile.getProperty("acf/_name"), is("Boeing 737-800"));
    }

    @Test
    void saveInsertsNewPropertyBeforePropertiesEnd() throws IOException {
        AcfFile acfFile = writeAcf(ACF_CONTENTS);

        acfFile.save(Map.of("acf/_callsign", "SWA123"));

        String saved = Files.readString(acfFile.getFile());
        assertThat(saved, containsString("P acf/_callsign SWA123\nPROPERTIES_END"));
        assertThat(saved, containsString("P acf/_name Boeing 737-800"));
        assertThat(acfFile.getProperty("acf/_callsign"), is("SWA123"));
    }

    @Test
    void saveWithEmptyChangesIsNoOp() throws IOException {
        AcfFile acfFile = writeAcf(ACF_CONTENTS);

        acfFile.save(Map.of());

        assertThat(Files.readString(acfFile.getFile()), is(ACF_CONTENTS));
    }

    @Test
    void saveKeepsBackupOfOriginalOnly() throws IOException {
        AcfFile acfFile = writeAcf(ACF_CONTENTS);
        Path backup = acfFile.getFile().resolveSibling("test.acf.bak");

        acfFile.save(Map.of("acf/_descrip", "Changed once"));
        acfFile.save(Map.of("acf/_descrip", "Changed twice"));

        assertThat(Files.readString(backup), is(ACF_CONTENTS));
        assertThat(acfFile.getProperty("acf/_descrip"), is("Changed twice"));
    }

    @Test
    void saveRejectsLineBreaksWithoutTouchingFile() throws IOException {
        AcfFile acfFile = writeAcf(ACF_CONTENTS);

        assertThrows(IllegalArgumentException.class,
                () -> acfFile.save(Map.of("acf/_descrip", "Bad\nP acf/_name injected")));
        assertThat(Files.readString(acfFile.getFile()), is(ACF_CONTENTS));
        assertThat(Files.exists(acfFile.getFile().resolveSibling("test.acf.bak")), is(false));
    }
}
