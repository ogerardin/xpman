package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.acf.AcfFileData;
import com.ogerardin.xplane.file.petitparser.AcfFileParser;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.nio.file.Path;
import java.util.Map;

/**
 * Represents a parsed X-Plane aircraft configuration file (.acf).
 *
 * <p>ACF files contain aircraft properties such as model name, version,
 * performance parameters, and system configurations. This class provides
 * convenient access to these properties.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * Path acfPath = Path.of("Aircraft/Laminar Research/Boeing B737-800/b738.acf");
 * AcfFile acfFile = new AcfFile(acfPath);
 *
 * // Get file version
 * String version = acfFile.getFileSpecVersion(); // "1100" or "1200"
 *
 * // Access individual property
 * String acfVersion = acfFile.getProperty("acf/_version");
 *
 * // Get all properties
 * Map<String, String> allProperties = acfFile.getProperties();
 * }</pre>
 *
 * <h2>File Format Reference</h2>
 * <p>See <a href="https://developer.x-plane.com/article/guidelines-for-working-with-text-based-acf-file-formats/">
 * X-Plane developer documentation</a> for ACF file format details.</p>
 *
 * @author Olivier G.
 * @see AcfFileData
 * @see AcfFileParser
 */
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true)
public class AcfFile extends XPlaneFile<AcfFileData> {

    /**
     * Create an AcfFile from a file path.
     *
     * @param file the path to the .acf file
     */
    public AcfFile(Path file) {
        super(file, new AcfFileParser());
    }

    /**
     * Get a specific property by name.
     *
     * @param name the property name (e.g., "acf/_version")
     * @return the property value, or null if not found
     */
    public String getProperty(String name) {
        return getProperties().get(name);
    }

    /**
     * Get all properties from the ACF file.
     *
     * @return unmodifiable map of property names to values
     */
    public Map<String, String> getProperties() {
        return getData().getProperties();
    }

    @ToString.Include
    private Path file() {
        return super.getFile();
    }

    /**
     * Returns the ACF file specification version.
     *
     * @return version string (e.g., "1100" for X-Plane 11, "1200" for X-Plane 12)
     */
    public String getFileSpecVersion() {
        return getData().getHeader().getSpecVersion();
    }

}
