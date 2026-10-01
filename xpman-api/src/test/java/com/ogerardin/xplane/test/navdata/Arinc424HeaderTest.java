package com.ogerardin.xplane.test.navdata;

import com.ogerardin.xplane.file.data.dat.Arinc424Header;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests reading the ARINC 424 header, which must work on the first lines only: these
 * files run to tens of megabytes.
 */
class Arinc424HeaderTest {

    /** Real HDR records from an FAA cycle, in the column layout the parser relies on. */
    private static final String FAA_HEADER =
            "HDR01FAACIFP18      001P013203969192610  09-SEP-202612:03:55  U.S.A. DOT FAA                                                113023A4\n" +
            "HDR02                                 FEDERAL AVIATION ADMINISTRATION                                                               \n" +
            "HDR03                                 AERONAUTICAL INFORMATION SERVICES                                                             \n" +
            "HDR04                                 CODED INSTRUMENT FLIGHT PROCEDURES VOLUME 2610  EFFECTIVE 01 OCT 2026                    \n" +
            "HDR05                                 REPORT DATA ERRORS TO FAA                 TEL 800 638 8972                                     \n" +
            "S   AS       N13W150          UNKUNK\n";

    @TempDir
    Path tempFolder;

    @Test
    void readsDatasetNameAndOriginator() throws IOException {
        Arinc424Header header = Arinc424Header.read(write("earth_424.dat", FAA_HEADER));

        assertThat(header.datasetName(), is("FAACIFP18"));
        assertThat(header.originator(), is("FEDERAL AVIATION ADMINISTRATION"));
        assertThat(header.isFaa(), is(true));
    }

    @Test
    void readsHeaderOfACommercialPublisher() throws IOException {
        String header = "HDR01NAVDAT48       001P013203969192610  09-SEP-202612:03:55  NAVDATA (AIRAC 424)                        \n" +
                "HDR02                                 GLOBAL NAVIGATION SERVICE                                           \n";

        Arinc424Header parsed = Arinc424Header.read(write("earth_424.dat", header));

        assertThat(parsed.datasetName(), is("NAVDAT48"));
        assertThat(parsed.isFaa(), is(false));
    }

    @Test
    void returnsNullForANonArinc424File() throws IOException {
        Arinc424Header header = Arinc424Header.read(write("earth_nav.dat", "I\n1100 version - data cycle 202610\n"));

        assertThat(header, is(nullValue()));
    }

    @Test
    void returnsNullForAnEmptyFile() throws IOException {
        assertThat(Arinc424Header.read(write("earth_424.dat", "")), is(nullValue()));
    }

    private Path write(String name, String contents) throws IOException {
        Path file = tempFolder.resolve(name);
        Files.writeString(file, contents);
        return file;
    }
}