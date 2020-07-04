package com.ogerardin.xplane.file.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Parsing result for a .acf file */
@Getter
@Setter
@ToString
public class SceneryPackIniData extends XPlaneFileData {

    final SceneryPackList sceneryPackList;

    public SceneryPackIniData(Header header, SceneryPackList sceneryPackList) {
        super(header);
        this.sceneryPackList = sceneryPackList;
    }
}
