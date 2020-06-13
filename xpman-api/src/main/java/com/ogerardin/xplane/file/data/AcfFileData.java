package com.ogerardin.xplane.file.data;

import lombok.*;

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
