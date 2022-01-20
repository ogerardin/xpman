package com.ogerardin.xplane.file.data.acf;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/** Parsing result for a .acf file */
@Getter
@Setter
@ToString
public class AcfFileData extends XPlaneFileData {

    final AcfProperties properties;

    public AcfFileData(Header header, AcfProperties properties) {
        super(header);
        this.properties = properties;
    }

    public static class AcfProperties extends HashMap<String, String> {
        public AcfProperties() {
        }

        public AcfProperties(Map<String, String> propertyMap) {
            super(propertyMap);
        }
    }
}
