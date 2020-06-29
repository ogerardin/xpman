package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.AcfFileData;
import com.ogerardin.xplane.file.grammar.AcfFileParser;

import java.nio.file.Path;
import java.util.Map;

/**
 * Represents a parsed .acf file
 */
public class AcfFile extends XPlaneDataFile<AcfFileParser, AcfFileData> {

    public AcfFile(Path file) {
        super(file, AcfFileParser.class);
    }

    public String getProperty(String name) {
        return getProperties().get(name);
    }

    public Map<String, String> getProperties() {
        return getData().getProperties();
    }



}
