package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.acf.AcfFileData;
import com.ogerardin.xplane.file.petitparser.AcfFileParser;
import lombok.ToString;

import java.nio.file.Path;
import java.util.Map;

/**
 * Represents a parsed .acf file
 */
@ToString(onlyExplicitlyIncluded = true)
public class AcfFile extends XPlaneFile<AcfFileData> {

    public AcfFile(Path file) {
        super(file, new AcfFileParser());
    }

    public String getProperty(String name) {
        return getProperties().get(name);
    }

    public Map<String, String> getProperties() {
        return getData().getProperties();
    }

    @ToString.Include
    private Path file() {
        return super.getFile();
    }



}
