package com.ogerardin.xplane.file.data.acf;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.*;

import java.util.HashMap;

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

    public static class AcfProperties extends HashMap<String, String> {}
}
