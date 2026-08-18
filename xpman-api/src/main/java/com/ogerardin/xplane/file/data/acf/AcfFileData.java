package com.ogerardin.xplane.file.data.acf;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Data model for parsed ACF (aircraft configuration) files.
 *
 * <p>ACF files contain aircraft properties as key-value pairs.
 * This class provides access to these properties via a HashMap.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * AcfFileData data = acfFile.getData();
 * String version = data.getProperties().get("acf/_version");
 * }</pre>
 *
 * @author Olivier G.
 * @see com.ogerardin.xplane.file.AcfFile
 */
@Getter
@Setter
@ToString
public class AcfFileData extends XPlaneFileData {

    /**
     * The aircraft properties as key-value pairs.
     */
    final AcfProperties properties;

    /**
     * Create an AcfFileData instance.
     *
     * @param header the file header
     * @param properties the aircraft properties
     */
    public AcfFileData(Header header, AcfProperties properties) {
        super(header);
        this.properties = properties;
    }

    /**
     * HashMap holding ACF properties.
     */
    public static class AcfProperties extends HashMap<String, String> {
        public AcfProperties() {
        }

        public AcfProperties(Map<String, String> propertyMap) {
            super(propertyMap);
        }
    }
}
