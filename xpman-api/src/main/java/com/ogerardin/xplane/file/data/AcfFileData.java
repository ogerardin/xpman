package com.ogerardin.xplane.file.data;

import lombok.*;

/** Parsing result for a .acf file */
@Getter
@Setter
@ToString
public class AcfFileData extends XPlaneFileData {

    final Properties properties;

    public AcfFileData(Header header, Properties properties) {
        super(header);
        this.properties = properties;
    }
}
