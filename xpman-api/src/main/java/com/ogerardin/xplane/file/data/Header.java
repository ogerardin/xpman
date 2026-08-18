package com.ogerardin.xplane.file.data;

import lombok.Data;

/**
 * Represents the header of an X-Plane file.
 *
 * <p>All X-Plane files start with a header containing:</p>
 * <ul>
 *   <li><b>Origin:</b> "I" (internal) or "A" (authored)</li>
 *   <li><b>Spec Version:</b> File format version (e.g., "1100", "1200")</li>
 *   <li><b>File Type:</b> Format identifier (e.g., "OBJ", "ACF")</li>
 * </ul>
 *
 * <h2>Example Header</h2>
 * <pre>
 * I
 * 1100 version
 * OBJ
 * </pre>
 *
 * @author Olivier G.
 */
@Data
public class Header {
    /**
     * The file origin: "I" for internal, "A" for authored.
     */
    final String origin;

    /**
     * The file format version (e.g., "1100" for X-Plane 11).
     */
    final String specVersion;

    /**
     * The file type identifier (e.g., "OBJ", "ACF").
     */
    final String fileType;
}
