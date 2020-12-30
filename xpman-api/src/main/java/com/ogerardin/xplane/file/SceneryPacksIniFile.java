package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.scenery.SceneryPackIniData;
import com.ogerardin.xplane.file.grammar.SceneryPacksIniParser;
import lombok.ToString;

import java.nio.file.Path;

/**
 * Represents a parsed scenery_packs.ini file
 */
@ToString(onlyExplicitlyIncluded = true)
public class SceneryPacksIniFile extends XPlaneFile<SceneryPacksIniParser, SceneryPackIniData> {

    public SceneryPacksIniFile(Path file) {
        super(file, SceneryPacksIniParser.class);
    }

    public SceneryPackIniData.SceneryPackList getSceneryPackList() {
        return getData().getSceneryPackList();
    }

    @ToString.Include
    private Path file() {
        return super.getFile();
    }



}
