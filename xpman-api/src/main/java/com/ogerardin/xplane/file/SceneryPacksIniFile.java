package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.scenery.SceneryPackIniData;
import com.ogerardin.xplane.file.petitparser.SceneryPacksIniParser;
import lombok.ToString;

import java.nio.file.Path;

/**
 * Represents a parsed scenery_packs.ini file (prioritized list of sceneries)
 */
@ToString(onlyExplicitlyIncluded = true)
public class SceneryPacksIniFile extends XPlaneFile<SceneryPackIniData> {

    public SceneryPacksIniFile(Path file) {
        super(file, new SceneryPacksIniParser());
    }

    public SceneryPackIniData.SceneryPackList getSceneryPackList() {
        return getData().getSceneryPackList();
    }

    @ToString.Include
    private Path file() {
        return super.getFile();
    }


    @Override
    public String getFileSpecVersion() {
        return getData().getHeader().getSpecVersion();
    }
}
