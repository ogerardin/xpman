package com.ogerardin.xplane.file.data.scenery;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.nio.file.Path;
import java.util.ArrayList;

/** Parsing result for a scenery_packs.ini file */
@Getter
@Setter
@ToString
public class SceneryPackIniData extends XPlaneFileData {

    final SceneryPackList sceneryPackList;

    public SceneryPackIniData(Header header, SceneryPackList sceneryPackList) {
        super(header);
        this.sceneryPackList = sceneryPackList;
    }

    public static class SceneryPackList extends ArrayList<Path> {}
}
