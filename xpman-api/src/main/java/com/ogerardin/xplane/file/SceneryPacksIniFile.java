package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.SceneryPackIniData;
import com.ogerardin.xplane.file.data.SceneryPackList;
import com.ogerardin.xplane.file.grammar.SceneryPacksIniParser;
import lombok.ToString;

import java.nio.file.Path;

/**
 * Represents a parsed scenery_packs.ini file
 */
@ToString(onlyExplicitlyIncluded = true)
public class SceneryPacksIniFile extends XPlaneDataFile<SceneryPacksIniParser, SceneryPackIniData> {

    public SceneryPacksIniFile(Path file) {
        super(file, SceneryPacksIniParser.class);
    }

    public SceneryPackList getSceneryPackList() {
        return getData().getSceneryPackList();
    }

    @ToString.Include
    private Path file() {
        return super.getFile();
    }



}
