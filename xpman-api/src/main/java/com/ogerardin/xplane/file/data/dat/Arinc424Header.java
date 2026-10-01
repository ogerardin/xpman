package com.ogerardin.xplane.file.data.dat;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The header of an ARINC 424 dataset, carried in the first five lines of the file as
 * {@code HDRnn} records in fixed-width columns.
 *
 * <pre>
 * HDR01FAACIFP18      001P013203969192610  09-SEP-202612:03:55  U.S.A. DOT FAA
 * </pre>
 *
 * <p>Only the dataset name (cols 6-15) and the originator (from col 42) are read, and
 * only the first few lines: an ARINC 424 master file is tens of megabytes, so parsing
 * must never pull the whole thing into memory.</p>
 *
 * @param datasetName the dataset's own name, e.g. {@code FAACIFP18}
 * @param originator  the organization that published the dataset, e.g. {@code U.S.A. DOT FAA}
 */
@Slf4j
public record Arinc424Header(String datasetName, String originator) {

    /** Column range of the record identifier ({@code HDRnn}), 1-based inclusive. */
    private static final int ID_END = 5;

    /** Column range of the dataset name, carried only by the first header record. */
    private static final int NAME_START = 6;
    private static final int NAME_END = 15;

    /**
     * Column at which the free-text part of the HDR02..HDR05 records starts. The HDR01
     * record instead carries a structured preamble from col 36 and is not used for this.
     */
    private static final int TEXT_START = 39;

    /** How many header lines to look at. */
    private static final int HEADER_LINES = 5;

    private static final String FIRST_ID = "HDR01";

    /**
     * Reads the header of an ARINC 424 file, reading at most the first
     * {@value #HEADER_LINES} lines.
     *
     * @return the header, or null if the file does not start with ARINC 424 header records
     */
    public static Arinc424Header read(Path file) {
        if (!Files.isReadable(file)) {
            return null;
        }
        String datasetName = null;
        String originator = null;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            for (int line = 0; line < HEADER_LINES; line++) {
                String text = reader.readLine();
                if (text == null) {
                    break;
                }
                String id = text.substring(0, Math.min(ID_END, text.length())).strip();
                if (!id.startsWith("HDR")) {
                    return null;
                }
                if (FIRST_ID.equals(id)) {
                    datasetName = field(text, NAME_START, NAME_END);
                } else if (originator == null) {
                    originator = freeText(text);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to read ARINC424 header of {}: {}", file, e.toString());
            return null;
        }
        return datasetName == null || datasetName.isEmpty() ? null : new Arinc424Header(datasetName, originator);
    }

    /** Returns the given 1-based inclusive column range of a record, stripped. */
    private static String field(String line, int from, int to) {
        if (line.length() < from) {
            return null;
        }
        String text = line.substring(from - 1, Math.min(to, line.length())).strip();
        return text.isEmpty() ? null : text;
    }

    /** Returns the free-text part of a header record, or null when it is blank. */
    private static String freeText(String line) {
        return field(line, TEXT_START, line.length());
    }

    /** True when this dataset was published by the FAA, whose data covers the US only. */
    public boolean isFaa() {
        return datasetName != null && datasetName.startsWith("FAACIFP");
    }
}