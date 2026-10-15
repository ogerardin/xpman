package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.acf.AcfFileData;
import com.ogerardin.xplane.file.petitparser.AcfFileParser;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern PROPERTY_NAME = Pattern.compile("[A-Za-z0-9_/,]+");

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
     * @return map of property names to values
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

    /**
     * Surgically write changed properties back to the .acf file, preserving other lines and
     * their order; new properties are inserted just before PROPERTIES_END. The original is
     * backed up once to a sibling {@code .bak}, and the in-memory property map is updated only
     * after a successful write.
     *
     * @param changes property name to new value; an empty map is a no-op
     * @throws IOException if the file cannot be read or written
     */
    @SneakyThrows
    public void save(Map<String, String> changes) {
        if (changes.isEmpty()) {
            return;
        }
        changes.forEach((name, value) -> {
            if (name == null || !PROPERTY_NAME.matcher(name).matches() || value == null
                    || value.contains("\n") || value.contains("\r")) {
                throw new IllegalArgumentException("Invalid ACF property: " + name);
            }
        });
        Path file = getFile();
        String content = Files.readString(file);
        for (Map.Entry<String, String> change : changes.entrySet()) {
            String name = change.getKey();
            String replacement = Matcher.quoteReplacement("P " + name + " " + change.getValue());
            Matcher matcher = Pattern.compile("^P " + Pattern.quote(name) + " .*$", Pattern.MULTILINE)
                    .matcher(content);
            if (matcher.find()) {
                content = matcher.replaceFirst(replacement);
            } else {
                // property not present in file: insert before PROPERTIES_END
                content = content.replace("PROPERTIES_END",
                        "P " + name + " " + change.getValue() + "\nPROPERTIES_END");
            }
        }
        Path backup = file.resolveSibling(file.getFileName() + ".bak");
        if (!Files.exists(backup)) {
            Files.copy(file, backup);
        }
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(tmp, content);
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        getProperties().putAll(changes);
    }

}
